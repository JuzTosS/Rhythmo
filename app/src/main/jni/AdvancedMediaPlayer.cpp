#include <jni.h>
#include <SLES/OpenSLES_AndroidConfiguration.h>
#include <SLES/OpenSLES.h>
#include <AndroidIO/SuperpoweredAndroidAudioIO.h>
#include "AdvancedMediaPlayer.h"
#include <SuperpoweredSimple.h>
#include <map>

static std::map<int, AdvancedMediaPlayer *> sPlayersMap;

static bool audioProcessing(void *clientdata, short int *audioIO, int numberOfSamples,
                            int __unused samplerate) {
    return ((AdvancedMediaPlayer *) clientdata)->process(audioIO,
                                                         (unsigned int) numberOfSamples);
}

void AdvancedMediaPlayer::playerEvent(void *__unused clientData,
                                      SuperpoweredAdvancedAudioPlayerEvent event, void *value) {
    switch (event) {
        case SuperpoweredAdvancedAudioPlayerEvent_LoadSuccess: {
            mIsPrepared = true;
            pthread_t t;
            pthread_create(&t, NULL, &AdvancedMediaPlayer::callOnPrepared, this);
        }
            break;
        case SuperpoweredAdvancedAudioPlayerEvent_LoadError:
            __android_log_print(ANDROID_LOG_DEBUG, "playerEvent", "Open error: %s",
                                (char *) value);

            mLastError = *((char *)value);
            pthread_t t;
            pthread_create(&t, NULL, &AdvancedMediaPlayer::callOnError, this);
            break;
        case SuperpoweredAdvancedAudioPlayerEvent_HLSNetworkError:
            __android_log_print(ANDROID_LOG_DEBUG, "playerEvent", "Network error: %s",
                                (char *) value);
            break;
        case SuperpoweredAdvancedAudioPlayerEvent_EOF: {
            this->pause();//Without a pause sometimes if UI thread is busy we can hear the beginning of the current song instead of the next
            pthread_t t;
            pthread_create(&t, NULL, &AdvancedMediaPlayer::callEndOfFile, this);
        }
            break;
        case SuperpoweredAdvancedAudioPlayerEvent_JogParameter:
//            __android_log_print(ANDROID_LOG_DEBUG, "playerEvent", "JogParameter: %s",
//                                (char *) value);
            break;
        case SuperpoweredAdvancedAudioPlayerEvent_DurationChanged:
//            __android_log_print(ANDROID_LOG_DEBUG, "playerEvent", "durationChanged: %s",
//                                (char *) value);
            break;
        default:
            __android_log_print(ANDROID_LOG_DEBUG, "playerEvent", "unknown event: %s",
                                (char *) value);
    };
}

static void playerEventCallback(void *__unused clientData,
                                SuperpoweredAdvancedAudioPlayerEvent event, void *value) {
    AdvancedMediaPlayer *player = (AdvancedMediaPlayer *) clientData;
    player->playerEvent(clientData, event, value);
}

AdvancedMediaPlayer::AdvancedMediaPlayer(unsigned int samplerate, unsigned int buffersize,
                                         JNIEnv *env, jobject *listener) {
    mListener = env->NewGlobalRef(*listener);
    jclass cls = env->GetObjectClass(*listener);
    mListenerClass = (jclass) env->NewGlobalRef(cls);

    env->GetJavaVM(&mJavaVM);

    stereoBuffer = (float *) malloc(sizeof(float) * 2 * buffersize + 128);
    mPlayer = new SuperpoweredAdvancedAudioPlayer(this,
                                                  playerEventCallback,
                                                  samplerate,
                                                  0);
    audioSystem = new SuperpoweredAndroidAudioIO(samplerate, buffersize, false, true,
                                                 audioProcessing, this, -1, SL_ANDROID_STREAM_MEDIA,
                                                 buffersize * 2);

    audioSystem->onBackground();
}

AdvancedMediaPlayer::~AdvancedMediaPlayer() {
    JNIEnv *env;
    mJavaVM->AttachCurrentThread(&env, NULL);
    env->DeleteGlobalRef(mListener);
    env->DeleteGlobalRef(mListenerClass);

    delete audioSystem;
    delete mPlayer;
    free(stereoBuffer);
}

bool AdvancedMediaPlayer::process(short int *output, unsigned int numberOfSamples) {
    bool silence = !mPlayer->process(stereoBuffer, false, numberOfSamples);
    if (!silence)
        SuperpoweredFloatToShortInt(stereoBuffer, output, numberOfSamples);

    return !silence;
}

void AdvancedMediaPlayer::setSource(const char *path) {
    mIsPrepared = false;
    audioSystem->onForeground();
    mPlayer->open(path);
}

unsigned int AdvancedMediaPlayer::getDuration() {
    return mPlayer->durationMs;
}

void AdvancedMediaPlayer::play() {
    if (mIsPrepared) {
        audioSystem->onForeground();
        mPlayer->play(false);
    }
    else
        __android_log_print(ANDROID_LOG_DEBUG, __func__, " is called before file is loaded");
}

void AdvancedMediaPlayer::pause() {
    if (mIsPrepared) {
        mPlayer->pause();
        audioSystem->onBackground();
    }
    else
        __android_log_print(ANDROID_LOG_DEBUG, __func__, " is called before file is loaded");
}

unsigned int AdvancedMediaPlayer::getPosition() {
    return (unsigned int) mPlayer->positionMs;
}

void AdvancedMediaPlayer::setPosition(unsigned int position) {
    if (mIsPrepared)
        mPlayer->setPosition(position, false, false);
    else
        __android_log_print(ANDROID_LOG_DEBUG, __func__, " is called before file is loaded");
}


void AdvancedMediaPlayer::setBPM(double bpm) {
    if (!mIsPrepared) {
        __android_log_print(ANDROID_LOG_DEBUG, __func__, " is called before file is loaded");
        return;
    }

//    if (bpm > 10.0) {
        mPlayer->setBpm(bpm);
//    }
//    else {
//        __android_log_print(ANDROID_LOG_DEBUG, "setBPM", "Bpm is less than 10.0, unable to do time stretching. Bpm is = %f",
//                            bpm);
//    }
}


void AdvancedMediaPlayer::setNewBPM(double bpm) {
    if (!mIsPrepared) {
        __android_log_print(ANDROID_LOG_DEBUG, __func__, " is called before file is loaded");
        return;
    }

    if (mPlayer->bpm > 10.0 && bpm > 10.0) {
        double tempo = bpm / mPlayer->bpm;
        mPlayer->setTempo(tempo, true);
    }
    else {
        mPlayer->setTempo(1, true);
//        __android_log_print(ANDROID_LOG_DEBUG, "setNewBPM", "Bpm is less than 10.0, unable to do time stretching. Bpm is = %f, new Bpm is = %f",
//                            mPlayer->bpm, bpm);
    }
}

static AdvancedMediaPlayer *getPlayer(jobject *instance, JNIEnv *env) {
    jclass cls = env->GetObjectClass(*instance);
    jmethodID method = env->GetMethodID(cls, "getIdJNI", "()I");
    int id = env->CallIntMethod(*instance, method);

    typedef std::map<int, AdvancedMediaPlayer *>::iterator it_type;
    for (it_type iterator = sPlayersMap.begin(); iterator != sPlayersMap.end(); iterator++) {
        if (id == iterator->first)
            return iterator->second;
    }

    __android_log_print(ANDROID_LOG_ERROR, __func__, "A player with id=%d not found!", id);
    return NULL;
}

extern "C" JNIEXPORT void Java_com_juztoss_rhythmo_audio_AdvancedMediaPlayer_releaseNative(JNIEnv *env, jobject instance) {
    jclass cls = env->GetObjectClass(instance);
    jmethodID method = env->GetMethodID(cls, "getIdJNI", "()I");
    int id = env->CallIntMethod(instance, method);

    AdvancedMediaPlayer *player = getPlayer(&instance, env);
    sPlayersMap.erase(id);
    delete player;
}

extern "C" JNIEXPORT void Java_com_juztoss_rhythmo_audio_AdvancedMediaPlayer_init(JNIEnv *env,
                                                                                    jobject instance,
                                                                                    jint samplerate,
                                                                                    jint buffersize) {
    AdvancedMediaPlayer *player = new AdvancedMediaPlayer((unsigned int) samplerate, (unsigned int) buffersize, env,
                                                          &instance);

    jclass cls = env->GetObjectClass(instance);
    jmethodID method = env->GetMethodID(cls, "getIdJNI", "()I");
    int id = env->CallIntMethod(instance, method);
    sPlayersMap.insert(std::pair<int, AdvancedMediaPlayer *>(id, player));

    //Musn't be called because we don't create more than one player
//    if(sPlayersMap.size() > 1)
//    {
//        env->ThrowNew(env->FindClass("java/lang/Exception"), "Created more than one player");
//    }
}

extern "C" JNIEXPORT void Java_com_juztoss_rhythmo_audio_AdvancedMediaPlayer_setSource(JNIEnv *env,
                                                                                         jobject instance,
                                                                                         jstring source) {
    if (source == NULL) {
        jclass cls = env->GetObjectClass(instance);
        jmethodID method = env->GetMethodID(cls, "onError", "()V");
        env->CallVoidMethod(instance, method);
        return;
    }
    const char *path = env->GetStringUTFChars(source, JNI_FALSE);
    AdvancedMediaPlayer *player = getPlayer(&instance, env);
    player->setSource(path);
    env->ReleaseStringUTFChars(source, path);

}

extern "C" JNIEXPORT void Java_com_juztoss_rhythmo_audio_AdvancedMediaPlayer_play(JNIEnv *env,
                                                                                    jobject instance) {
    AdvancedMediaPlayer *player = getPlayer(&instance, env);
    player->play();
}

extern "C" JNIEXPORT void Java_com_juztoss_rhythmo_audio_AdvancedMediaPlayer_pause(JNIEnv *env,
                                                                                     jobject instance) {
    AdvancedMediaPlayer *player = getPlayer(&instance, env);
    player->pause();
}

extern "C" JNIEXPORT jint Java_com_juztoss_rhythmo_audio_AdvancedMediaPlayer_getDuration(JNIEnv *env,
                                                                                           jobject instance) {
    AdvancedMediaPlayer *player = getPlayer(&instance, env);
    return (jint) player->getDuration();
}

extern "C" JNIEXPORT jint Java_com_juztoss_rhythmo_audio_AdvancedMediaPlayer_getPosition(JNIEnv *env,
                                                                                           jobject instance) {
    AdvancedMediaPlayer *player = getPlayer(&instance, env);
    return (jint) player->getPosition();
}

extern "C" JNIEXPORT void Java_com_juztoss_rhythmo_audio_AdvancedMediaPlayer_setPosition(JNIEnv *env,
                                                                                           jobject instance,
                                                                                           jint offset) {
    AdvancedMediaPlayer *player = getPlayer(&instance, env);
    player->setPosition((unsigned int) offset);
}

extern "C" JNIEXPORT void Java_com_juztoss_rhythmo_audio_AdvancedMediaPlayer_setBPM(JNIEnv *env,
                                                                                      jobject instance,
                                                                                      jdouble bpm) {
    AdvancedMediaPlayer *player = getPlayer(&instance, env);
    player->setBPM((double) bpm);

}

extern "C" JNIEXPORT void Java_com_juztoss_rhythmo_audio_AdvancedMediaPlayer_setNewBPM(JNIEnv *env,
                                                                                         jobject instance,
                                                                                         jdouble bpm) {
    AdvancedMediaPlayer *player = getPlayer(&instance, env);
    player->setNewBPM((double) bpm);

}
