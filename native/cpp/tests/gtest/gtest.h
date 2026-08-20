// SPDX-License-Identifier: MIT
// tests/gtest/gtest.h
//
// Thin include-shim that lets tests/test_hash_table.cpp (which does
// `#include <gtest/gtest.h>`) compile and run against the local gtest_lite
// implementation when the real GoogleTest distribution is not available.
//
// In the production CMake build (cmake/HashTable.cmake with
// LEXORA_HASH_TABLE_BUILD_TESTS=ON), GoogleTest is fetched via FetchContent
// and its include directory is added to the target BEFORE this one, so
// `#include <gtest/gtest.h>` resolves to the real GoogleTest header. The
// shim is never seen in that build.
//
// For the standalone smoke build:
//   g++ -std=c++17 -pthread -Iinclude -Itests
//       -DLEXORA_USE_GTEST_LITE_MAIN
//       tests/test_hash_table.cpp src/hash_table.cpp -o test_hash_table
#ifndef LEXORA_TESTS_GTEST_SHIM_H
#define LEXORA_TESTS_GTEST_SHIM_H
#include "../gtest_lite.hpp"
#endif  // LEXORA_TESTS_GTEST_SHIM_H
