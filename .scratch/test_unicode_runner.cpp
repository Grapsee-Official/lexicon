// test_unicode_runner.cpp — minimal main() wrapper so we can link and run
// tests/test_unicode.cpp against the bundled gtest shim, which doesn't
// provide its own main().
#include "gtest/gtest.h"
int main(int argc, char** argv) {
    testing::InitGoogleTest(&argc, argv);
    return RUN_ALL_TESTS();
}
