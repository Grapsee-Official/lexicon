#!/usr/bin/env bash
# =============================================================================
#  build-all.sh — Linux host build + test driver for the Lexora C++ engine.
#
#  Task P3-FIX-14.
#
#  This script builds the entire Phase 3 C++ backend (liblexora-engine.so +
#  the GoogleTest binary) on a plain Linux host, runs the test suite if gtest
#  is available, and prints a single summary at the end.
#
#  It does NOT require the Android NDK or an iOS toolchain — those cross
#  builds remain the responsibility of cmake/Toolchain-Android.cmake and
#  cmake/Toolchain-iOS.cmake. Use this script for fast iteration / CI on a
#  Linux dev box or sandbox to verify the engine compiles and its unit tests
#  pass before invoking the heavier cross-compilation flow.
#
#  Usage:
#    ./build-all.sh                # incremental build + (optional) test run
#    ./build-all.sh --clean        # wipe build-linux/ before configuring
#    ./build-all.sh --no-tests     # build only, skip the test step entirely
#    ./build-all.sh --jobs N       # override parallel job count (default: nproc)
#
#  Exit codes:
#    0  — engine built AND tests passed (or were skipped/ignored)
#    1  — engine build OR test run failed
#    2  — environment problem (missing cmake / g++ / libsqlite3-dev)
#
# =============================================================================
set -euo pipefail

# -----------------------------------------------------------------------------
# Globals populated as steps run; consumed by the EXIT trap's summary.
# -----------------------------------------------------------------------------
SCRIPT_NAME="build-all.sh"
# CURRENT_STEP: the step we are currently executing (for the ERR trap to
#   surface in the summary). Reset to "(none)" after each step completes
#   successfully so the summary can distinguish "we are mid-step and
#   something failed" from "all steps finished cleanly".
CURRENT_STEP="(none)"
# FAILED_STEP / FAILED_LINE: populated ONLY when a step actually fails.
FAILED_STEP="(none)"
FAILED_LINE=0
ANY_FAILURE=0
ENGINE_BUILT=0
ENGINE_ARTIFACT=""
TESTS_SKIPPED=0          # gtest not installed (legitimate)
TESTS_DISABLED=0         # user passed --no-tests
TESTS_BUILT=0
TESTS_PASSED=0
TESTS_FAILED_COUNT=""
START_EPOCH="$(date +%s)"

# -----------------------------------------------------------------------------
# Pretty logging helpers (ANSI color; degrades fine on non-tty).
# -----------------------------------------------------------------------------
if [ -t 1 ]; then
    C_BLUE=$'\033[1;34m';  C_GREEN=$'\033[1;32m'
    C_YELLOW=$'\033[1;33m'; C_RED=$'\033[1;31m'
    C_CYAN=$'\033[1;36m';   C_BOLD=$'\033[1m'
    C_RESET=$'\033[0m'
else
    C_BLUE=""; C_GREEN=""; C_YELLOW=""; C_RED=""; C_CYAN=""; C_BOLD=""; C_RESET=""
fi

log()   { printf '%s[%s]%s %s\n' "$C_BLUE"  "$SCRIPT_NAME" "$C_RESET" "$*"; }
ok()    { printf '%s[ ok ]%s %s\n'           "$C_GREEN"  "$C_RESET" "$*"; }
warn()  { printf '%s[warn]%s %s\n'           "$C_YELLOW" "$C_RESET" "$*" >&2; }
err()   { printf '%s[fail]%s %s\n'           "$C_RED"    "$C_RESET" "$*" >&2; }
hdr()   { printf '\n%s==== %s ====%s\n'      "$C_CYAN"   "$*" "$C_RESET"; }

# -----------------------------------------------------------------------------
# ERR / EXIT traps.
#
# ERR fires whenever a command exits non-zero (and we are about to abort
# because of `set -e`). We record WHICH step failed so the EXIT trap can
# surface it in the summary.
#
# EXIT fires on every exit path (success OR failure), so the summary is
# always the last thing the user sees — including when the engine build
# itself fails (in which case the test step is never reached and is
# reported as "skipped (engine build failed)" in the summary).
# -----------------------------------------------------------------------------
on_err() {
    # Capture $? FIRST — any other command (including the parameter
    # expansion below) would clobber it.
    local rc=$?
    FAILED_STEP="$CURRENT_STEP"
    FAILED_LINE="${1:-0}"
    ANY_FAILURE=1
    err "step '$CURRENT_STEP' failed at line ${FAILED_LINE} (exit ${rc})"
}
trap 'on_err $LINENO' ERR

on_exit() {
    local rc=$?
    local elapsed=$(( $(date +%s) - START_EPOCH ))
    local mins=$(( elapsed / 60 ))
    local secs=$(( elapsed % 60 ))

    printf '\n'
    printf '%s========================================%s\n' "$C_CYAN" "$C_RESET"
    printf '%s  BUILD SUMMARY  %s\n' "$C_BOLD" "$C_RESET"
    printf '%s========================================%s\n' "$C_CYAN" "$C_RESET"

    # Overall: success only if no failure was recorded AND exit code is 0.
    # ANY_FAILURE is the authoritative flag (more reliable than $rc, which
    # can be 0 even after a non-fatal abort path, e.g. signal exit).
    if [ "$ANY_FAILURE" -eq 0 ] && [ "$rc" -eq 0 ]; then
        printf '  Overall:        %sSUCCESS%s\n' "$C_GREEN" "$C_RESET"
    else
        printf '  Overall:        %sFAILED%s  (exit %s, last failed step: %s, line: %s)\n' \
               "$C_RED" "$C_RESET" "$rc" "$FAILED_STEP" "$FAILED_LINE"
    fi
    printf '  Elapsed:        %dm%02ds\n' "$mins" "$secs"

    # Engine
    if [ "$ENGINE_BUILT" -eq 1 ] && [ -n "$ENGINE_ARTIFACT" ] && [ -f "$ENGINE_ARTIFACT" ]; then
        local size
        size=$(du -h "$ENGINE_ARTIFACT" 2>/dev/null | cut -f1)
        printf '  Engine:         %sbuilt%s  -> %s (%s)\n' \
               "$C_GREEN" "$C_RESET" "$ENGINE_ARTIFACT" "$size"
    else
        printf '  Engine:         %sNOT built%s\n' "$C_RED" "$C_RESET"
    fi

    # Tests
    if [ "$TESTS_DISABLED" -eq 1 ]; then
        printf '  Tests:          %sskipped%s  (--no-tests)\n' "$C_YELLOW" "$C_RESET"
    elif [ "$ENGINE_BUILT" -eq 0 ]; then
        printf '  Tests:          %sskipped%s  (engine build failed)\n' "$C_YELLOW" "$C_RESET"
    elif [ "$TESTS_SKIPPED" -eq 1 ]; then
        printf '  Tests:          %sskipped%s  (gtest not available)\n' "$C_YELLOW" "$C_RESET"
    elif [ "$TESTS_BUILT" -eq 0 ]; then
        printf '  Tests:          %snot built%s  (gtest available but no test target — CMake bug?)\n' "$C_YELLOW" "$C_RESET"
    elif [ "$TESTS_PASSED" -eq 1 ]; then
        printf '  Tests:          %sPASSED%s\n' "$C_GREEN" "$C_RESET"
    else
        printf '  Tests:          %sFAILED%s%s\n' \
               "$C_RED" "$C_RESET" "${TESTS_FAILED_COUNT:+ ($TESTS_FAILED_COUNT failing)}"
    fi

    printf '%s========================================%s\n' "$C_CYAN" "$C_RESET"

    # Hint for the most common remediation paths.
    if [ "$TESTS_SKIPPED" -eq 1 ]; then
        printf '\nTo enable the test step, install GoogleTest:\n'
        printf '  Debian/Ubuntu:  sudo apt-get install libgtest-dev libgmock-dev\n'
        printf '                  sudo apt-get install cmake pkg-config        # if missing\n'
        printf '  Fedora:         sudo dnf install gtest-devel gmock-devel\n'
    fi
    if [ "$ENGINE_BUILT" -eq 0 ] || { [ "$TESTS_BUILT" -eq 0 ] && [ "$TESTS_SKIPPED" -eq 0 ] && [ "$TESTS_DISABLED" -eq 0 ] && [ "$ENGINE_BUILT" -eq 1 ]; }; then
        printf '\nRe-run with verbose make output for diagnosis:\n'
        printf '  %s --clean      # wipe build-linux/ first\n' "$0"
        printf '  cmake --build %s --verbose\n' "$BUILD_DIR"
    fi
    printf '\n'
}
trap on_exit EXIT

# -----------------------------------------------------------------------------
# Path resolution.
# -----------------------------------------------------------------------------
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
NATIVE_ROOT="$SCRIPT_DIR"
CPP_ROOT="$NATIVE_ROOT/cpp"
BUILD_DIR="$CPP_ROOT/build-linux"

if [ ! -d "$CPP_ROOT" ] || [ ! -f "$CPP_ROOT/CMakeLists.txt" ]; then
    err "C++ source tree not found at: $CPP_ROOT"
    err "Expected: $CPP_ROOT/CMakeLists.txt"
    FAILED_STEP="path-resolution"
    FAILED_LINE=$LINENO
    ANY_FAILURE=1
    exit 2
fi

# -----------------------------------------------------------------------------
# Arg parsing.
# -----------------------------------------------------------------------------
CLEAN=0
RUN_TESTS=1
JOBS=""
while [ $# -gt 0 ]; do
    case "$1" in
        --clean|-c)
            CLEAN=1
            ;;
        --no-tests)
            RUN_TESTS=0
            ;;
        --jobs|-j)
            shift
            JOBS="${1:-}"
            if ! [[ "$JOBS" =~ ^[0-9]+$ ]] || [ "$JOBS" -lt 1 ]; then
                err "--jobs requires a positive integer (got: '${JOBS}')"
                FAILED_STEP="arg-parse"
                FAILED_LINE=$LINENO
                ANY_FAILURE=1
                exit 2
            fi
            ;;
        --jobs=*)
            JOBS="${1#--jobs=}"
            if ! [[ "$JOBS" =~ ^[0-9]+$ ]] || [ "$JOBS" -lt 1 ]; then
                err "--jobs requires a positive integer (got: '${JOBS}')"
                FAILED_STEP="arg-parse"
                FAILED_LINE=$LINENO
                ANY_FAILURE=1
                exit 2
            fi
            ;;
        --help|-h)
            # Print the header comment block, then exit cleanly WITHOUT
            # running the EXIT-trap summary (it would be misleading for
            # a --help invocation: nothing was built).
            trap - EXIT
            sed -n '2,/^# =\+/p' "$0" | sed 's/^# \?//'
            exit 0
            ;;
        *)
            err "unknown argument: $1"
            err "run '$0 --help' for usage"
            FAILED_STEP="arg-parse"
            FAILED_LINE=$LINENO
            ANY_FAILURE=1
            exit 2
            ;;
    esac
    shift
done

# Default parallelism: nproc (fallback 4 on systems without it).
if [ -z "$JOBS" ]; then
    JOBS="$(nproc 2>/dev/null || echo 4)"
fi

# -----------------------------------------------------------------------------
# Step 0: preflight — verify required tools and SQLite3 are installed.
# -----------------------------------------------------------------------------
CURRENT_STEP="preflight"
hdr "Step 0 — Preflight toolchain check"

require_cmd() {
    local cmd="$1"
    if ! command -v "$cmd" >/dev/null 2>&1; then
        err "missing required tool: $cmd"
        err "  install with: sudo apt-get install ${cmd}  (or your distro's equivalent)"
        FAILED_STEP="preflight"
        FAILED_LINE=$LINENO
        ANY_FAILURE=1
        exit 2
    fi
}
require_cmd cmake
require_cmd make
require_cmd g++
require_cmd pkg-config

ok "cmake:       $(cmake --version | head -1 | sed 's/^cmake version //')"
ok "make:        $(make --version | head -1)"
ok "g++:         $(g++ --version | head -1)"
ok "pkg-config:  $(pkg-config --version)"
ok "jobs:        $JOBS"

# SQLite3 is REQUIRED by the engine (find_package(SQLite3 REQUIRED) in
# CMakeLists.txt). Fail fast with a clear remediation hint instead of
# letting CMake emit its own terse error.
if ! pkg-config --exists sqlite3; then
    err "libsqlite3-dev is NOT installed (pkg-config cannot find sqlite3)"
    err "  install with: sudo apt-get install libsqlite3-dev"
    FAILED_STEP="preflight"
    FAILED_LINE=$LINENO
    ANY_FAILURE=1
    exit 2
fi
ok "sqlite3:     $(pkg-config --modversion sqlite3)"

# Detect GoogleTest via multiple methods. We DO NOT fail if it's missing —
# the engine still builds; only the test step is skipped (with a warning).
GTEST_AVAILABLE=0
GTEST_SOURCE="(unknown)"

if pkg-config --exists gtest_main; then
    GTEST_AVAILABLE=1
    GTEST_SOURCE="system (pkg-config gtest_main $(pkg-config --modversion gtest_main))"
elif pkg-config --exists gtest; then
    GTEST_AVAILABLE=1
    GTEST_SOURCE="system (pkg-config gtest $(pkg-config --modversion gtest))"
elif [ -f /usr/include/gtest/gtest.h ] && \
     { [ -f /usr/lib/x86_64-linux-gnu/libgtest.so ] || \
       [ -f /usr/lib/x86_64-linux-gnu/libgtest.a ]  || \
       [ -f /usr/lib/*/libgtest.so ] || \
       [ -f /usr/lib/*/libgtest.a ]; }; then
    GTEST_AVAILABLE=1
    GTEST_SOURCE="system (/usr/include/gtest/gtest.h + libgtest)"
fi

if [ "$GTEST_AVAILABLE" -eq 1 ]; then
    ok "gtest:       $GTEST_SOURCE"
elif [ "$RUN_TESTS" -eq 1 ]; then
    warn "gtest:       NOT installed — test step will be skipped"
    warn "             install with: sudo apt-get install libgtest-dev libgmock-dev"
else
    warn "gtest:       NOT installed (--no-tests given, so no impact)"
fi

# -----------------------------------------------------------------------------
# Step 1: configure (cmake).
# -----------------------------------------------------------------------------
CURRENT_STEP="configure"
hdr "Step 1 — Configure (cmake)"

if [ "$CLEAN" -eq 1 ]; then
    log "clean: wiping $BUILD_DIR"
    rm -rf "$BUILD_DIR"
fi

log "source dir: $CPP_ROOT"
log "build dir:  $BUILD_DIR"

# Always pass -DBUILD_TESTS=ON even if gtest is missing — the top-level
# CMakeLists.txt has a built-in find_package(GTest QUIET) guard that skips
# the test target gracefully when gtest is absent, so the configure step
# succeeds in either case.
#
# Wrap in `if !` so we control the exit code (cmake may return codes other
# than 1 on failure; our contract is: any failure -> exit 1).
if ! cmake -S "$CPP_ROOT" -B "$BUILD_DIR" \
        -DCMAKE_BUILD_TYPE=Release \
        -DBUILD_SHARED_LIBS=ON \
        -DBUILD_TESTS=ON \
        -DTARGET_ANDROID=OFF \
        -DTARGET_IOS=OFF \
        -DCMAKE_EXPORT_COMPILE_COMMANDS=ON; then
    err "cmake configure failed"
    FAILED_STEP="configure"
    FAILED_LINE=$LINENO
    ANY_FAILURE=1
    exit 1
fi
ok "configure complete"
CURRENT_STEP="(none)"

# -----------------------------------------------------------------------------
# Step 2: build (cmake --build / make).
# -----------------------------------------------------------------------------
CURRENT_STEP="build"
hdr "Step 2 — Build (cmake --build, -j$JOBS)"

if ! cmake --build "$BUILD_DIR" --parallel "$JOBS"; then
    err "cmake --build failed"
    FAILED_STEP="build"
    FAILED_LINE=$LINENO
    ANY_FAILURE=1
    exit 1
fi
ok "build complete"
CURRENT_STEP="(none)"

# Locate the engine artifact. With BUILD_SHARED_LIBS=ON, the .so lands
# directly in the build dir (liblexora-engine.so). Probe a few likely spots.
for candidate in \
    "$BUILD_DIR/liblexora-engine.so" \
    "$BUILD_DIR/libliblexora-engine.so" \
    "$BUILD_DIR/lib/lexora-engine.so" \
    "$BUILD_DIR/lib/liblexora-engine.so"; do
    if [ -f "$candidate" ]; then
        ENGINE_BUILT=1
        ENGINE_ARTIFACT="$candidate"
        break
    fi
done

# Fall back to a directory walk if the standard locations missed it.
if [ "$ENGINE_BUILT" -eq 0 ]; then
    found="$(find "$BUILD_DIR" -maxdepth 4 -type f \
                \( -name 'liblexora-engine.so' -o -name 'liblexora-engine.a' \) \
                -print -quit 2>/dev/null || true)"
    if [ -n "$found" ] && [ -f "$found" ]; then
        ENGINE_BUILT=1
        ENGINE_ARTIFACT="$found"
    fi
fi

if [ "$ENGINE_BUILT" -eq 1 ]; then
    ok "engine artifact: $ENGINE_ARTIFACT"
else
    err "engine built but no liblexora-engine.{so,a} found under $BUILD_DIR"
    FAILED_STEP="artifact-locate"
    FAILED_LINE=$LINENO
    ANY_FAILURE=1
    exit 1
fi

# -----------------------------------------------------------------------------
# Step 3: tests (ctest) — only if gtest is available AND --no-tests wasn't
# passed AND the engine actually built.
#
# Tests may LEGITIMATELY fail (test suite finds a real bug). We deliberately
# disable `set -e` around the ctest invocation so that a test failure does
# NOT abort the script before the summary can be printed. We capture the
# exit code manually and feed it into the summary.
# -----------------------------------------------------------------------------
if [ "$RUN_TESTS" -eq 0 ]; then
    hdr "Step 3 — Tests (skipped: --no-tests)"
    TESTS_DISABLED=1
elif [ "$GTEST_AVAILABLE" -eq 0 ]; then
    hdr "Step 3 — Tests (skipped: gtest not installed)"
    TESTS_SKIPPED=1
else
    CURRENT_STEP="test"
    hdr "Step 3 — Tests (ctest)"

    # The top-level CMakeLists.txt only enables testing() if find_package(GTest)
    # succeeded. So if we got here (GTEST_AVAILABLE=1) but CMake didn't find
    # gtest (e.g. headers present but no .so), ctest will report "No tests
    # found". Handle that case explicitly.
    if [ ! -f "$BUILD_DIR/CTestTestfile.cmake" ]; then
        warn "no CTestTestfile.cmake in build dir — CMake did not register a test target."
        warn "(most likely: gtest headers exist but libgtest does not; CMake's"
        warn " find_package(GTest QUIET) requires both)."
        warn "Install the matching -dev package (libgtest-dev on Debian/Ubuntu)."
        TESTS_SKIPPED=1
    else
        # Locate the test binary. With the top-level CMakeLists.txt's inline
        # test logic, the executable lands directly in the build dir as
        # lexora-engine-tests. If tests/CMakeLists.txt was wired in via
        # add_subdirectory, it lands under build/tests/. Probe both.
        TEST_BIN=""
        for candidate in \
            "$BUILD_DIR/lexora-engine-tests" \
            "$BUILD_DIR/tests/lexora-engine-tests" \
            "$BUILD_DIR/bin/lexora-engine-tests"; do
            if [ -x "$candidate" ]; then
                TEST_BIN="$candidate"
                break
            fi
        done
        if [ -z "$TEST_BIN" ]; then
            TEST_BIN="$(find "$BUILD_DIR" -maxdepth 4 -type f -name 'lexora-engine-tests' -executable -print -quit 2>/dev/null || true)"
        fi

        if [ -n "$TEST_BIN" ] && [ -x "$TEST_BIN" ]; then
            TESTS_BUILT=1
            ok "test binary: $TEST_BIN"
            log "running ctest --output-on-failure ..."

            # Disable set -e for the ctest invocation so a test failure is
            # captured (not fatal to the script). Re-enable afterwards.
            set +e
            ctest --test-dir "$BUILD_DIR" --output-on-failure --parallel "$JOBS"
            ctest_rc=$?
            set -e

            if [ "$ctest_rc" -eq 0 ]; then
                ok "all tests passed"
                TESTS_PASSED=1
            else
                err "ctest reported failures (exit $ctest_rc)"
                # Try to extract a "X% tests passed, Y tests failed out of Z"
                # summary line from ctest's output for the build summary.
                failed_line="$(ctest --test-dir "$BUILD_DIR" 2>&1 \
                    | grep -E '[0-9]+% tests passed' | tail -1 || true)"
                if [ -n "$failed_line" ]; then
                    TESTS_FAILED_COUNT="$failed_line"
                fi
            fi
        else
            warn "gtest is available but no lexora-engine-tests binary was built."
            warn "(this typically means find_package(GTest) failed at configure time"
            warn " even though gtest files exist — check the configure log above.)"
        fi
    fi
fi

# -----------------------------------------------------------------------------
# Done. EXIT trap will print the summary and choose the right exit code.
# Choose final exit code based on what happened:
#   - engine build failure: already exited via set -e (won't reach here)
#   - test failure: exit 1
#   - everything else (incl. tests skipped): exit 0
# -----------------------------------------------------------------------------
if [ "$TESTS_BUILT" -eq 1 ] && [ "$TESTS_PASSED" -eq 0 ]; then
    FAILED_STEP="test"
    FAILED_LINE=$LINENO
    ANY_FAILURE=1
    exit 1
fi

ok "build-all.sh: done."
exit 0
