cmake_minimum_required(VERSION 3.0)
project(token-ring C)

file(GLOB_RECURSE sources src/main/*.c src/main/*.h)

add_executable(token-ring ${sources} ${data})

target_compile_options(token-ring PUBLIC -std=c++1y -Wall)

set(CPACK_PACKAGE_NAME "agh-ds-token-ring")
set(CPACK_PACKAGE_VERSION "1.0.0")

set(CPACK_MONOLITHIC_INSTALL 1)

include(CPack)
