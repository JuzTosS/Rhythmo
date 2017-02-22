#include <jni.h>
#include <android/log.h>
#include <algorithmfactory.h>
#include <essentiamath.h>
#include <scheduler/network.h>
#include <SuperpoweredDecoder.h>
#include <pool.h>
#include <streaming/algorithms/poolstorage.h>
#include <streaming/algorithms/vectoroutput.h>
#include <streaming/algorithms/vectorinput.h>
#include <SuperpoweredSimple.h>

using namespace std;
using namespace essentia;
using namespace essentia::streaming;
using namespace essentia::scheduler;

extern "C" JNIEXPORT jdouble Java_com_juztoss_rhythmo_audio_BpmDetector_detect(JNIEnv *env,
                                                                               jobject instance,
                                                                               jstring source
) {

    essentia::init();

    AlgorithmFactory &factory = AlgorithmFactory::instance();

    const char *path = env->GetStringUTFChars(source, JNI_FALSE);
    __android_log_print(ANDROID_LOG_ERROR, __func__, " Start decode, path %s", path);



    vector<Real> audioStream;

//:::::::::::::::::::::::::::::::::::::::

    SuperpoweredDecoder *decoder = new SuperpoweredDecoder();
    const char *openError = decoder->open(path, false, 0, 0);


    if (openError) {
        __android_log_print(ANDROID_LOG_ERROR, __func__, " Decoder error, path %s, error: %s", path, openError);
        delete decoder;
        env->ReleaseStringUTFChars(source, path);
        return -1;
    };
    env->ReleaseStringUTFChars(source, path);


    __android_log_print(ANDROID_LOG_ERROR, __func__, " Samplerate: %i", decoder->samplerate);

    const int samplesToDetect = 2097152 / 16; //The pow of 2 in case the fourier transform inside of the detector need it
    int64_t duration = decoder->durationSamples;
    int64_t start = duration / 2 - samplesToDetect / 2;
    int64_t end = start + samplesToDetect;

    if(start <= 0)
    {
        start = 0;
        end = decoder->durationSamples;
    }

    decoder->seekTo(start, false);
    // Create a buffer for the 16-bit integer samples coming from the decoder.
    short int *intBuffer = (short int *)malloc(decoder->samplesPerFrame * 2 * sizeof(short int) + 16384);
    // Create a buffer for the 32-bit floating point samples required by the effect.
    float *floatBuffer = (float *)malloc(decoder->samplesPerFrame * 2 * sizeof(float) + 1024);

    // Processing.
    while (decoder->samplePosition <= end) {
        // Decode one frame. samplesDecoded will be overwritten with the actual decoded number of samples.
        unsigned int samplesDecoded = decoder->samplesPerFrame;
        if (decoder->decode(intBuffer, &samplesDecoded) == SUPERPOWEREDDECODER_ERROR) break;
        if (samplesDecoded < 1) break;

        // Convert the decoded PCM samples from 16-bit integer to 32-bit floating point.
        SuperpoweredShortIntToFloat(intBuffer, floatBuffer, samplesDecoded);

        // Submit samples to the analyzer.
        for(int i = 0; i < samplesDecoded; i++)
        {
            audioStream.push_back(*(floatBuffer + i * sizeof(float)));
        }
    };


    __android_log_print(ANDROID_LOG_ERROR, __func__, " Vector length: %ld", audioStream.size());



//:::::::::::::::::::::::::::::::::::::::


    Pool poolResults;
    VectorInput<Real> *audioStreamV = new VectorInput<Real>(&audioStream);

    Algorithm *rhythmextractor = factory.create("RhythmExtractor2013",
                                                    "method", "multifeature");
//                                                    "method", "degara");

    audioStreamV->output("data") >> rhythmextractor->input("signal");
    rhythmextractor->output("bpm") >> PoolConnector(poolResults, "rhythm.bpm");
    rhythmextractor->output("ticks") >> NOWHERE;
    rhythmextractor->output("confidence") >> NOWHERE;
    rhythmextractor->output("estimates") >> NOWHERE;
    rhythmextractor->output("bpmIntervals") >> NOWHERE;


    try {
        Network network(audioStreamV);
        network.run();
    }catch (EssentiaException e)
    {
        __android_log_print(ANDROID_LOG_ERROR, __func__, "ERROR");
    }



    Real bpm = poolResults.value<Real>("rhythm.bpm");

    bpm = bpm * 44100 / decoder->samplerate;

    __android_log_print(ANDROID_LOG_ERROR, __func__, " RESULT IS %f", bpm);

//    delete rhythmextractor;
    delete decoder;
    essentia::shutdown();


    return bpm;
}