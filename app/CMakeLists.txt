# 编译本地库时我们需要的最小的cmake版本
cmake_minimum_required(VERSION 3.4.1)

#if(${ANDROID_ABI} STREQUAL "armeabi-v7a")
#   include_directories(${ANDROID_SYSROOT}/usr/include/arm-linux-androideabi)
#elseif(${ANDROID_ABI} STREQUAL "arm64-v8a")
#    include_directories(${ANDROID_SYSROOT}/usr/include/aarch64-linux-android)
#else()
#    include_directories(${ANDROID_SYSROOT}/usr/include/arm-linux-androideabi)
#endif()

# C 的编译选项是 CMAKE_C_FLAGS
# 指定编译参数，可选
# SET(CMAKE_CXX_FLAGS "-Wno-error=format-security -Wno-error=pointer-sign")
# SET(CMAKE_CXX_FLAGS "-isystem ${ANDROID_SYSROOT}/usr/include/arm-linux-androideabi")

# include_directories(${ANDROID_SYSROOT}/usr/include/arm-linux-androideabi)

#设置生成的so动态库最后输出的路径
# set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI})

add_library( native-lib
             SHARED
             src/main/cpp/dameon-lib.cpp
             src/main/cpp/native-lib.cpp )

target_link_libraries( native-lib
                       log )