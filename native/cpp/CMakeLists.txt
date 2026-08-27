cmake_minimum_required(VERSION 3.22)
project(lexicon-engine LANGUAGES C CXX VERSION 1.0.0)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

# Options
option(BUILD_SHARED_LIBS "Build shared library" ON)
option(BUILD_TESTS "Build Google Test" ON)
option(TARGET_ANDROID "Cross-compile for Android NDK" OFF)
option(TARGET_IOS "Cross-compile for iOS" OFF)

# Default build type
if(NOT CMAKE_BUILD_TYPE)
    set(CMAKE_BUILD_TYPE Release)
endif()

# Platform flags
add_compile_options(-O2 -Wall -Wextra)

# Find SQLite3
find_package(SQLite3 REQUIRED)

# Collect sources
file(GLOB LEXICON_SOURCES ${CMAKE_CURRENT_SOURCE_DIR}/src/*.cpp)
file(GLOB LEXICON_HEADERS ${CMAKE_CURRENT_SOURCE_DIR}/include/lexicon/*.hpp ${CMAKE_CURRENT_SOURCE_DIR}/include/lexicon/*.h)

# Main library target
add_library(lexicon-engine ${LEXICON_SOURCES})
target_include_directories(lexicon-engine PUBLIC
    ${CMAKE_CURRENT_SOURCE_DIR}/include
)
target_link_libraries(lexicon-engine PUBLIC SQLite::SQLite3)

# For Android: also build as shared .so
if(TARGET_ANDROID)
    set_target_properties(lexicon-engine PROPERTIES
        OUTPUT_NAME lexicon-engine
        SUFFIX .so
    )
endif()

# For iOS: build as framework
if(TARGET_IOS)
    set_target_properties(lexicon-engine PROPERTIES
        FRAMEWORK TRUE
        FRAMEWORK_VERSION 1
        OUTPUT_NAME LexiconEngine
        MACOSX_FRAMEWORK_IDENTIFIER com.lexicon.engine
    )
endif()

# Tests
if(BUILD_TESTS)
    enable_testing()
    # Try to find Google Test
    find_package(GTest QUIET)
    if(NOT GTest_FOUND)
        message(STATUS "Google Test not found. Tests will be skipped.")
        message(STATUS "To enable tests: install gtest or run cmake with -DFETCHCONTENT_SOURCE_DIR_GOOGLETEST=/path/to/gtest")
    else()
        file(GLOB LEXICON_TEST_SOURCES ${CMAKE_CURRENT_SOURCE_DIR}/tests/test_*.cpp)
        add_executable(lexicon-engine-tests ${LEXICON_TEST_SOURCES})
        target_link_libraries(lexicon-engine-tests PRIVATE lexicon-engine GTest::GTest GTest::Main)
        include(GoogleTest)
        gtest_discover_tests(lexicon-engine-tests)
    endif()
endif()

# Install
include(GNUInstallDirs)
install(TARGETS lexicon-engine
    LIBRARY DESTINATION ${CMAKE_INSTALL_LIBDIR}
    ARCHIVE DESTINATION ${CMAKE_INSTALL_LIBDIR}
    FRAMEWORK DESTINATION ${CMAKE_INSTALL_LIBDIR}
)
install(DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/include/lexicon
    DESTINATION ${CMAKE_INSTALL_INCLUDEDIR}
)
