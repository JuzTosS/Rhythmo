#include <jni.h>
#include <SuperpoweredAnalyzer.h>
#include <SuperpoweredDecoder.h>
#include <android/log.h>
#include <malloc.h>
#include <SuperpoweredSimple.h>
#include <list>
#include <cmath>
#include <functional>

float getBpmFromMeasures(std::list<float> sorted_list) {
    float sameValueThreshold = 5.0;

    int resultMaxRepeats = 0;
    float resultMaxRepeatsValue = 0;

    int maxRepeats = 0;
    float maxRepeatsValue = 0;

    std::list<float>::const_iterator listIt;
    for (listIt = sorted_list.begin(); listIt != sorted_list.end(); listIt++) {
        float bpm = *listIt;
        if(bpm <= 0) continue;

        if (std::abs(bpm - maxRepeatsValue) > sameValueThreshold) {
            maxRepeats = 0;
            maxRepeatsValue = 0;
        }

        maxRepeatsValue = (maxRepeatsValue * maxRepeats + bpm) / (maxRepeats + 1);
        maxRepeats++;

        if (maxRepeats > resultMaxRepeats) {
            resultMaxRepeats = maxRepeats;
            resultMaxRepeatsValue = maxRepeatsValue;
        }

    }

    return resultMaxRepeatsValue;
}

void insertSorted(std::list<float> &sorted_list, float value)
{
    std::list<float>::const_iterator it = std::lower_bound( sorted_list.begin(), sorted_list.end(), value, std::greater<float>() );
    sorted_list.insert(it, value);
}

float getBpm(SuperpoweredDecoder *decoder, int64_t start, int64_t end) {

    if (start <= 0) {
        start = 0;
        end = decoder->durationSamples;
    }

    decoder->seekTo(start, false);
    SuperpoweredOfflineAnalyzer *analyzer = new SuperpoweredOfflineAnalyzer(decoder->samplerate, 0, decoder->durationSeconds);

    // Create a buffer for the 16-bit integer samples coming from the decoder.
    short int *intBuffer = (short int *) malloc(decoder->samplesPerFrame * 2 * sizeof(short int) + 16384);
    // Create a buffer for the 32-bit floating point samples required by the effect.
    float *floatBuffer = (float *) malloc(decoder->samplesPerFrame * 2 * sizeof(float) + 1024);

    while (decoder->samplePosition <= end) {
        // Decode one frame. samplesDecoded will be overwritten with the actual decoded number of samples.
        unsigned int samplesDecoded = decoder->samplesPerFrame;
        if (decoder->decode(intBuffer, &samplesDecoded) == SUPERPOWEREDDECODER_ERROR) break;
        if (samplesDecoded < 1) break;

        // Convert the decoded PCM samples from 16-bit integer to 32-bit floating point.
        SuperpoweredShortIntToFloat(intBuffer, floatBuffer, samplesDecoded);

        // Submit samples to the analyzer.
        analyzer->process(floatBuffer, samplesDecoded);
    };

    float bpm;
    analyzer->getresults(NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, &bpm, NULL, NULL);
    return bpm;
}

extern "C" JNIEXPORT jdouble Java_com_juztoss_rhythmo_audio_BpmDetector_detect(JNIEnv *env,
                                                                               jobject instance,
                                                                               jstring source
) {
    const char *path = env->GetStringUTFChars(source, JNI_FALSE);
    SuperpoweredDecoder *decoder = new SuperpoweredDecoder();
    const char *openError = decoder->open(path, false, 0, 0);

    if (openError) {
        __android_log_print(ANDROID_LOG_ERROR, __func__, " Decoder error, path %s, error: %s", path, openError);
        delete decoder;
        env->ReleaseStringUTFChars(source, path);
        return -1;
    };
    env->ReleaseStringUTFChars(source, path);

    const float batchLengthInSeconds = 3.0;
    const int numberOfPieces = 6;
    int batchLengthInSamples = (int) (batchLengthInSeconds * decoder->samplerate);

    int64_t duration = decoder->durationSamples;


    std::list<float> bpmList;
    for(int i = 1; i <= numberOfPieces; i++)
    {
        int64_t start = duration / (numberOfPieces + 1) * i;
        float bpm = getBpm(decoder, start, start + batchLengthInSamples);
        insertSorted(bpmList, bpm);

    }

    // Cleanup.
    delete decoder;

    return getBpmFromMeasures(bpmList);
}
