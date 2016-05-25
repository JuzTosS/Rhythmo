#include <jni.h>
#include <SLES/OpenSLES_AndroidConfiguration.h>
#include <SLES/OpenSLES.h>
#include <AndroidIO/SuperpoweredAndroidAudioIO.h>
#include "AdvancedMediaPlayer.h"
#include <SuperpoweredSimple.h>
#include <android/log.h>

static AdvancedMediaPlayer *sPlayer = NULL;

static bool audioProcessing(void *clientdata, short int *audioIO, int numberOfSamples,
                            int __unused samplerate) {
    return ((AdvancedMediaPlayer *) clientdata)->process(audioIO,
                                                         (unsigned int) numberOfSamples);
}

void AdvancedMediaPlayer::playerEvent(void *clientData,
                                      SuperpoweredAdvancedAudioPlayerEvent event,
                                      void *value) {
    switch (event) {
        case SuperpoweredAdvancedAudioPlayerEvent_LoadSuccess: {
            mIsPrepared = true;

            JNIEnv *env;
            mJavaVM->AttachCurrentThread(&env, NULL);
            jmethodID method = env->GetMethodID(mListenerClass, "onPrepared", "()V");
            env->CallVoidMethod(mListener, method);
            mJavaVM->DetachCurrentThread();
        }
            break;
        case SuperpoweredAdvancedAudioPlayerEvent_LoadError:
            __android_log_print(ANDROID_LOG_DEBUG, "playerEvent", "Open error: %s",
                                (char *) value);
            {
                JNIEnv *env;
                mJavaVM->AttachCurrentThread(&env, NULL);
                jmethodID method = env->GetMethodID(mListenerClass, "onError",
                                                    "(Ljava/lang/String;)V");
                env->CallVoidMethod(mListener, method, env->NewStringUTF((char *) value));
                mJavaVM->DetachCurrentThread();
            }
            break;
        case SuperpoweredAdvancedAudioPlayerEvent_NetworkError:
            __android_log_print(ANDROID_LOG_DEBUG, "playerEvent", "Network error: %s",
                                (char *) value);
            break;
        case SuperpoweredAdvancedAudioPlayerEvent_EOF: {
            JNIEnv *env;
            mJavaVM->AttachCurrentThread(&env, NULL);
            jmethodID method = env->GetMethodID(mListenerClass, "onEnd", "()V");
            env->CallVoidMethod(mListener, method);
            mJavaVM->DetachCurrentThread();
        }
            break;
        case SuperpoweredAdvancedAudioPlayerEvent_JogParameter:
            __android_log_print(ANDROID_LOG_DEBUG, "playerEvent", "JogParameter: %s",
                                (char *) value);
            break;
        case SuperpoweredAdvancedAudioPlayerEvent_DurationChanged:
            __android_log_print(ANDROID_LOG_DEBUG, "playerEvent", "durationChanged: %s",
                                (char *) value);
            break;
        default:;
    };
}

static void playerEventCallback(void *__unused clientData,
                                SuperpoweredAdvancedAudioPlayerEvent event, void *value) {
    sPlayer->playerEvent(clientData, event, value);
}

AdvancedMediaPlayer::AdvancedMediaPlayer(unsigned int samplerate, unsigned int buffersize,
                                         JNIEnv *env, jobject *listener) {
    mListener = env->NewGlobalRef(*listener);
    jclass cls = env->GetObjectClass(*listener);
    mListenerClass = (jclass) env->NewGlobalRef(cls);

    env->GetJavaVM(&mJavaVM);

    stereoBuffer = (float *) malloc(sizeof(float) * 2 * buffersize + 128);
    mPlayer = new SuperpoweredAdvancedAudioPlayer(&mPlayer,
                                                  playerEventCallback,
                                                  samplerate,
                                                  0);
    audioSystem = new SuperpoweredAndroidAudioIO(samplerate, buffersize, false, true,
                                                 audioProcessing, this, -1, SL_ANDROID_STREAM_MEDIA,
                                                 buffersize * 2);
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
    mPlayer->open(path);
}

unsigned int AdvancedMediaPlayer::getDuration() {
    return mPlayer->durationMs;
}

void AdvancedMediaPlayer::play() {
    if(mIsPrepared)
        mPlayer->play(false);
    else
        __android_log_print(ANDROID_LOG_DEBUG, __func__, " is called before file is loaded");
}

void AdvancedMediaPlayer::pause() {
    if(mIsPrepared)
        mPlayer->pause();
    else
        __android_log_print(ANDROID_LOG_DEBUG, __func__, " is called before file is loaded");
}

unsigned int AdvancedMediaPlayer::getPosition() {
    return (unsigned int) mPlayer->positionMs;
}

void AdvancedMediaPlayer::setPosition(unsigned int position) {
    if(mIsPrepared)
        mPlayer->setPosition(position, false, false);
    else
        __android_log_print(ANDROID_LOG_DEBUG, __func__, " is called before file is loaded");
}


void AdvancedMediaPlayer::setBPM(double bpm) {
    if(mIsPrepared)
        mPlayer->setBpm(bpm);
    else
        __android_log_print(ANDROID_LOG_DEBUG, __func__, " is called before file is loaded");
}


void AdvancedMediaPlayer::setNewBPM(double bpm) {
    if(!mIsPrepared)
    {
        __android_log_print(ANDROID_LOG_DEBUG, __func__, " is called before file is loaded");
        return;
    }

    if (mPlayer->bpm > 10.0) {
        double tempo = bpm / mPlayer->bpm;
        mPlayer->setTempo(tempo, true);
    }
    else {
        __android_log_print(ANDROID_LOG_DEBUG, "setNewBPM", "Bpm is less than 10.0, unable to do time stretching. Bpm is = %f",
                            mPlayer->bpm);
    }
}


extern "C" JNIEXPORT void Java_com_juztoss_bpmplayer_AdvancedMediaPlayer_init(JNIEnv *env,
                                                                              jobject instance,
                                                                              jint samplerate,
                                                                              jint buffersize) {


    sPlayer = new AdvancedMediaPlayer((unsigned int) samplerate, (unsigned int) buffersize, env,
                                      &instance);
}

extern "C" JNIEXPORT void Java_com_juztoss_bpmplayer_AdvancedMediaPlayer_setSource(JNIEnv *env,
                                                                                   jobject instance,
                                                                                   jstring source) {
    const char *path = env->GetStringUTFChars(source, JNI_FALSE);
    sPlayer->setSource(path);
    env->ReleaseStringUTFChars(source, path);

}

extern "C" JNIEXPORT void Java_com_juztoss_bpmplayer_AdvancedMediaPlayer_play(JNIEnv *env,
                                                                              jobject instance) {
    sPlayer->play();
}

extern "C" JNIEXPORT void Java_com_juztoss_bpmplayer_AdvancedMediaPlayer_pause(JNIEnv *env,
                                                                               jobject instance) {
    sPlayer->pause();
}

extern "C" JNIEXPORT jint Java_com_juztoss_bpmplayer_AdvancedMediaPlayer_getDuration(JNIEnv *env,
                                                                                     jobject instance) {
    return (jint) sPlayer->getDuration();
}

extern "C" JNIEXPORT jint Java_com_juztoss_bpmplayer_AdvancedMediaPlayer_getPosition(JNIEnv *env,
                                                                                     jobject instance) {
    return (jint) sPlayer->getPosition();
}

extern "C" JNIEXPORT void Java_com_juztoss_bpmplayer_AdvancedMediaPlayer_setPosition(JNIEnv *env,
                                                                                     jobject instance,
                                                                                     jint offset) {
    sPlayer->setPosition((unsigned int) offset);
}

extern "C" JNIEXPORT void Java_com_juztoss_bpmplayer_AdvancedMediaPlayer_setBPM(JNIEnv *env,
                                                                                jobject instance,
                                                                                jdouble bpm) {
    sPlayer->setBPM((double) bpm);

}

extern "C" JNIEXPORT void Java_com_juztoss_bpmplayer_AdvancedMediaPlayer_setNewBPM(JNIEnv *env,
                                                                                   jobject instance,
                                                                                   jdouble bpm) {
    sPlayer->setNewBPM((double) bpm);

}
