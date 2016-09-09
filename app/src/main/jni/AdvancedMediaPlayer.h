#ifndef Header_AdvancedMediaPlayer
#define Header_AdvancedMediaPlayer

#include <stdlib.h>
#include <SuperpoweredAdvancedAudioPlayer.h>
#include <pthread.h>
#include <android/log.h>
#include <unistd.h>

class AdvancedMediaPlayer {
public:
    AdvancedMediaPlayer(unsigned int samplerate, unsigned int buffersize, JNIEnv *env,
                        jobject *listener);

    ~AdvancedMediaPlayer();

    void setSource(const char *string);

    unsigned int getDuration();

    void play();

    void pause();

    unsigned int getPosition();

    void playerEvent(void *clientData, SuperpoweredAdvancedAudioPlayerEvent event,
                     void *__unused value);

    void setPosition(unsigned int position);

    bool process(short int *output, unsigned int numberOfSamples);

    void setBPM(double bpm);

    void setNewBPM(double bpm);

public:
    //READ ONLY
    jobject mListener;
    jclass mListenerClass;
    JavaVM *mJavaVM = NULL;
    char mLastError;
private:
    SuperpoweredAdvancedAudioPlayer *mPlayer;
    SuperpoweredAndroidAudioIO *audioSystem;
    float *stereoBuffer;

    bool mIsPrepared = false;


public:

    static void *callOnPrepared(void *context) {
        AdvancedMediaPlayer *player = (AdvancedMediaPlayer *) context;
        JNIEnv *env;
        player->mJavaVM->AttachCurrentThread(&env, NULL);
        jmethodID method = env->GetMethodID(player->mListenerClass, "onPrepared", "()V");
        env->CallVoidMethod(player->mListener, method);
        player->mJavaVM->DetachCurrentThread();
        pthread_exit(NULL);
    }

    static void *callEndOfFile(void *context) {
        AdvancedMediaPlayer *player = (AdvancedMediaPlayer *) context;
        JNIEnv *env;
        player->mJavaVM->AttachCurrentThread(&env, NULL);
        jmethodID method = env->GetMethodID(player->mListenerClass, "onEnd", "()V");
        env->CallVoidMethod(player->mListener, method);
        player->mJavaVM->DetachCurrentThread();
        pthread_exit(NULL);
    }

    static void *callOnError(void *context) {
        AdvancedMediaPlayer *player = (AdvancedMediaPlayer *) context;
        JNIEnv *env;
        player->mJavaVM->AttachCurrentThread(&env, NULL);
        jmethodID method = env->GetMethodID(player->mListenerClass, "onError",
                                            "(Ljava/lang/String;)V");
        env->CallVoidMethod(player->mListener, method, env->NewStringUTF(&player->mLastError));
        player->mJavaVM->DetachCurrentThread();
        pthread_exit(NULL);
    }
};

#endif