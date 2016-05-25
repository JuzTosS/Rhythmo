#ifndef Header_AdvancedMediaPlayer
#define Header_AdvancedMediaPlayer

#include <stdlib.h>
#include <SuperpoweredAdvancedAudioPlayer.h>
#include <pthread.h>

class BPMAdvancedMediaPlayer {
public:
    BPMAdvancedMediaPlayer(unsigned int samplerate, unsigned int buffersize, JNIEnv *env,
                           jobject *listener);

    ~BPMAdvancedMediaPlayer();

    void setSource(const char *string);

    unsigned int getDuration();

    void play();

    void pause();

    unsigned int getPosition();

    void playerEvent(void *clientData, SuperpoweredAdvancedAudioPlayerEvent event,
                     void *__unused value);

    void setPosition(unsigned int position);

    bool process(short int *output, unsigned int numberOfSamples);

private:
    SuperpoweredAdvancedAudioPlayer *mPlayer;
    SuperpoweredAndroidAudioIO *audioSystem;
    jobject mListener;
    jclass mListenerClass;
    JavaVM *mJavaVM = NULL;
    float *stereoBuffer;

    bool mIsPrepared;
};

#endif