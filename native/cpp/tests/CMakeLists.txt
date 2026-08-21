# =============================================================================
#  tests/CMakeLists.txt — Google Test binary for lexora-engine.
#
#  Builds a single test binary `lexora-engine-tests` that aggregates every
#  test_*.cpp in this directory, links it against lexora-engine + gtest +
#  gtest_main, and registers each individual TEST() with CTest via
#  gtest_discover_tests() (the modern replacement for the older
#  GTEST_ADD_TEST macro).
#
#  GoogleTest acquisition strategy (mirrors cmake/GoogleTest.cmake):
#    1. If real GoogleTest is available (find_package or FetchContent), use
#       it — link gtest + gtest_main, no special defines needed.
#    2. Otherwise, fall back to the bundled tests/gtest_lite.hpp shim:
#       add tests/ to the include path so <gtest/gtest.h> resolves to the
#       shim at tests/gtest/gtest.h, and define LEXORA_USE_GTEST_LITE_MAIN
#       so the test source file provides main(). No gtest link library
#       needed — the shim is header-only.
#
#  Optional: -DLEXORA_PER_FILE_TESTS=ON builds one binary per test file, in
#  addition to the aggregate binary (useful for isolating failures).
# =============================================================================
file(GLOB LEXORA_TEST_SOURCES CONFIGURE_DEPENDS "${CMAKE_CURRENT_LIST_DIR}/test_*.cpp")

if(NOT LEXORA_TEST_SOURCES)
    message(STATUS "No test_*.cpp files found — skipping test binary.")
    return()
endif()

list(LENGTH LEXORA_TEST_SOURCES _NSRC)
message(STATUS "Discovering ${_NSRC} test source file(s) under ${CMAKE_CURRENT_LIST_DIR}")

# -----------------------------------------------------------------------------
# GoogleTest acquisition (real or shim)
# -----------------------------------------------------------------------------
if(LEXORA_GTEST_AVAILABLE)
    # Real GoogleTest was found by cmake/GoogleTest.cmake.
    set(_lexora_test_link ${LEXORA_GTEST_LIBS})
    set(_lexora_test_defs "")
    set(_lexora_test_inc "")
    set(_lexora_test_discovers ON)
else()
    # Fall back to the bundled gtest_lite shim (header-only, no link lib).
    message(STATUS "GoogleTest not available — using tests/gtest_lite.hpp shim.")
    set(_lexora_test_link "")
    set(_lexora_test_defs LEXORA_USE_GTEST_LITE_MAIN)
    # The shim is reachable as <gtest/gtest.h> via tests/gtest/gtest.h.
    set(_lexora_test_inc ${CMAKE_CURRENT_LIST_DIR})
    # gtest_discover_tests() requires the real gtest binary's --gtest_list_tests
    # output, which the shim does not produce. Register the whole binary as a
    # single CTest entry instead.
    set(_lexora_test_discovers OFF)
endif()

# -----------------------------------------------------------------------------
# Aggregate test binary: lexora-engine-tests
# -----------------------------------------------------------------------------
add_executable(lexora-engine-tests ${LEXORA_TEST_SOURCES})

target_include_directories(lexora-engine-tests PRIVATE
    ${CMAKE_SOURCE_DIR}/include
    ${_lexora_test_inc})

target_compile_definitions(lexora-engine-tests PRIVATE ${_lexora_test_defs})

# Match the engine's strict warning level so test code stays clean.
if(CMAKE_CXX_COMPILER_ID MATCHES "Clang|AppleClang|GNU")
    target_compile_options(lexora-engine-tests PRIVATE
        -Wall -Wextra -Wno-unused-parameter)
endif()

target_link_libraries(lexora-engine-tests PRIVATE
    lexora-engine
    ${_lexora_test_link})

if(_lexora_test_discovers)
    # Register every TEST() in the binary as a CTest entry. PRE_TEST mode
    # means CMake re-discovers the test list at ctest-time.
    include(GoogleTest)
    gtest_discover_tests(lexora-engine-tests
        WORKING_DIRECTORY ${CMAKE_CURRENT_LIST_DIR}
        DISCOVERY_MODE PRE_TEST
        PROPERTIES
            LABELS "lexora;unit"
            TIMEOUT 30)
else()
    # Shim mode: register the whole binary as a single CTest entry.
    add_test(NAME lexora-engine-tests
             COMMAND lexora-engine-tests
             WORKING_DIRECTORY ${CMAKE_CURRENT_LIST_DIR})
    set_tests_properties(lexora-engine-tests PROPERTIES
        LABELS "lexora;unit"
        TIMEOUT 60)
endif()

# -----------------------------------------------------------------------------
# Optional: one binary per test_*.cpp (for isolated failure analysis)
# -----------------------------------------------------------------------------
if(LEXORA_PER_FILE_TESTS)
    foreach(_src ${LEXORA_TEST_SOURCES})
        get_filename_component(_name ${_src} NAME_WE)
        add_executable(${_name} ${_src})
        target_include_directories(${_name} PRIVATE
            ${CMAKE_SOURCE_DIR}/include
            ${_lexora_test_inc})
        target_compile_definitions(${_name} PRIVATE ${_lexora_test_defs})
        target_link_libraries(${_name} PRIVATE
            lexora-engine ${_lexora_test_link})
        if(_lexora_test_discovers)
            gtest_discover_tests(${_name}
                WORKING_DIRECTORY ${CMAKE_CURRENT_LIST_DIR}
                DISCOVERY_MODE PRE_TEST
                PROPERTIES LABELS "lexora;unit" TIMEOUT 30)
        else()
            add_test(NAME ${_name} COMMAND ${_name}
                     WORKING_DIRECTORY ${CMAKE_CURRENT_LIST_DIR})
        endif()
    endforeach()
endif()
