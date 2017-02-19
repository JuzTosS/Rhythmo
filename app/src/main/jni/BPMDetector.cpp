#include <jni.h>
#include <SuperpoweredAnalyzer.h>
#include <SuperpoweredDecoder.h>
#include <android/log.h>
#include <malloc.h>
#include <SuperpoweredSimple.h>
#include <sys/stat.h>

long getFileSize(const char *filename) {
    struct stat st;

    if (stat(filename, &st) == 0)
        return st.st_size;

    return -1;
}

//TODO: decrease memory consumption for each song.
//1. Detect the file size
//2. Detect the file length in bytes
//3. Get the file size that enough for the detection
//4. Read the file from the middle

extern "C" JNIEXPORT jdouble Java_com_juztoss_rhythmo_audio_BpmDetector_detect(JNIEnv *env,
                                                                               jobject instance,
                                                                               jstring source
) {

    const bool DEBUG = false;

    const char *path = env->GetStringUTFChars(source, JNI_FALSE);
    if(DEBUG) __android_log_print(ANDROID_LOG_ERROR, __func__, " Start decode, path %s", path);

    SuperpoweredDecoder *decoder = new SuperpoweredDecoder();
    const char *openError = decoder->open(path, true, 0, 0);
    if(DEBUG) __android_log_print(ANDROID_LOG_ERROR, __func__, " Start decode, path %ld; %f; %i", decoder->durationSamples, decoder->durationSeconds, decoder->samplesPerFrame);

    if (openError) {
        __android_log_print(ANDROID_LOG_ERROR, __func__, " Decoder error, path %s, error: %s", path, openError);
        delete decoder;
        env->ReleaseStringUTFChars(source, path);
        return -1;
    };

    double durationSeconds = decoder->durationSeconds;
    delete decoder;

    long fileSize = getFileSize(path);
    if(DEBUG) __android_log_print(ANDROID_LOG_ERROR, __func__, " File size: %ld", fileSize);

    if(durationSeconds <= 0 || fileSize <= 0)
    {
        __android_log_print(ANDROID_LOG_ERROR, __func__, " Empty file, path %s", path);
        env->ReleaseStringUTFChars(source, path);
        return -1;
    }


    const int minSecondsToDetect = 5;
    double bytesPerSecond = (float)fileSize / durationSeconds;

    int readingOffset = 0;
    int readingLength = int(minSecondsToDetect * bytesPerSecond);

    if(durationSeconds > minSecondsToDetect)
    {
        readingOffset = int(fileSize / 2) - readingLength / 2;
    }
    else
    {
        readingLength = 0;
    }

    decoder = new SuperpoweredDecoder();
    if(DEBUG) __android_log_print(ANDROID_LOG_ERROR, __func__, " Open with: %d, %d", readingOffset, readingLength);
    openError = decoder->open(path, false, readingOffset, readingLength);
    if (openError) {
        __android_log_print(ANDROID_LOG_ERROR, __func__, " Decoder error (2), path %s, error: %s", path, openError);
        delete decoder;
        env->ReleaseStringUTFChars(source, path);
        return -1;
    };

    env->ReleaseStringUTFChars(source, path);

    SuperpoweredOfflineAnalyzer * analyzer = new SuperpoweredOfflineAnalyzer(decoder->samplerate, 0, decoder->durationSeconds);

    // Create a buffer for the 16-bit integer samples coming from the decoder.
    short int *intBuffer = (short int *)malloc(decoder->samplesPerFrame * 2 * sizeof(short int) + 16384);
    // Create a buffer for the 32-bit floating point samples required by the effect.
    float *floatBuffer = (float *)malloc(decoder->samplesPerFrame * 2 * sizeof(float) + 1024);

    // Processing.
    while (decoder->samplePosition <= decoder->durationSamples) {
        // Decode one frame. samplesDecoded will be overwritten with the actual decoded number of samples.
        unsigned int samplesDecoded = decoder->samplesPerFrame;
        if (decoder->decode(intBuffer, &samplesDecoded) == SUPERPOWEREDDECODER_ERROR) break;
        if (samplesDecoded < 1) break;

        // Convert the decoded PCM samples from 16-bit integer to 32-bit floating point.
        SuperpoweredShortIntToFloat(intBuffer, floatBuffer, samplesDecoded);

        // Submit samples to the analyzer.
        analyzer->process(floatBuffer, samplesDecoded);
    };

    // Get the result.
    float bpm;
    analyzer->getresults(NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, &bpm,  NULL, NULL);

    // Cleanup.
    delete decoder;
    delete analyzer;
    free(intBuffer);
    free(floatBuffer);

    return bpm;
}