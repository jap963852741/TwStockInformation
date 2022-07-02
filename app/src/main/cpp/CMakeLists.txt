# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)


find_package(curl REQUIRED CONFIG)
find_package(jsoncpp REQUIRED CONFIG)
find_library(log-lib log)
add_library(app
        SHARED
        app.cpp http.cpp logging.cpp appv2.cpp)

set_target_properties(app PROPERTIES CXX_STANDARD 17)

target_compile_options(app PRIVATE -Wall -Werror)

target_link_libraries(app
        ${log-lib}
        curl::curl
        jsoncpp::jsoncpp)
