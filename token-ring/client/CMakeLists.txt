cmake_minimum_required(VERSION 3.0)
project(token-ring C)

file(GLOB_RECURSE sources src/main/*.c src/main/*.h)

add_executable(token-ring ${sources} ${data})

target_compile_options(token-ring PUBLIC -Wall)

add_compile_definitions(_GNU_SOURCE)

# pthreads

find_package(Threads REQUIRED)
target_link_libraries(token-ring Threads::Threads)

# CPack

set(CPACK_PACKAGE_NAME "agh-ds-token-ring")
set(CPACK_PACKAGE_VERSION "1.0.0")
set(CPACK_MONOLITHIC_INSTALL 1)

include(CPack)
