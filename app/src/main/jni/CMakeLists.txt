cmake_minimum_required(VERSION 3.4.1)

message(${ANDROID_ABI})

file(GLOB CPP_FILES "*.cpp")


add_library(
	AdvancedMediaPlayer
	SHARED
	${CPP_FILES}
	${PATH_TO_SUPERPOWERED}/AndroidIO/SuperpoweredAndroidAudioIO.cpp
)

include_directories(src/main/jni)
include_directories(${PATH_TO_SUPERPOWERED})
include_directories(${PATH_TO_ESSENTIA})

target_link_libraries(
    AdvancedMediaPlayer
    log
    android
    OpenSLES
    ${PATH_TO_SUPERPOWERED}/libSuperpoweredAndroid${ANDROID_ABI}.a
    ${PATH_TO_ESSENTIA}/libessentia.a
)