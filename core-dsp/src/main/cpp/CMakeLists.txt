cmake_minimum_required(VERSION 3.22.1)
project(onyx_dsp)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_library(onyx_dsp SHARED
    native-lib.cpp
    onyx_dsp_engine.cpp
)

find_library(log-lib log)

target_link_libraries(onyx_dsp ${log-lib})
