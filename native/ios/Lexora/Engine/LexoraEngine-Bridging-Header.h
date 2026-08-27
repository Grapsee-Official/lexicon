//  LexiconEngine-Bridging-Header.h
//  Import this in the Xcode project's "Objective-C Bridging Header" build
//  setting (SWIFT_OBJC_BRIDGING_HEADER = Lexicon/Engine/LexiconEngine-Bridging-Header.h).
//  This exposes the C ABI to Swift.
//
//  Path resolution
//  ---------------
//  The import path below is relative to the iOS project root
//  (/native/ios/Lexicon/), NOT to this file's directory. From the project
//  root, `../../cpp/include/lexicon/c_api.h` resolves to
//  `/native/cpp/include/lexicon/c_api.h` — the canonical location of the
//  C ABI header shipped by the C++ engine.
//
//  For this relative path to resolve at compile time, the Xcode target's
//  HEADER_SEARCH_PATHS must include:
//      $(SRCROOT)/Lexicon
//  (Clang will then look up "../../cpp/include/lexicon/c_api.h" starting from
//  /native/ios/Lexicon/ and find /native/cpp/include/lexicon/c_api.h.)
//
//  Alternatively set HEADER_SEARCH_PATHS to include
//  `$(SRCROOT)/../cpp/include` and replace the #import below with
//  `#import "lexicon/c_api.h"` — both forms work.

#ifndef LEXICON_ENGINE_BRIDGING_HEADER_H
#define LEXICON_ENGINE_BRIDGING_HEADER_H

// Pull in the C ABI: LexiconEngineHandle, LexiconWordEntry, LexiconSearchResults,
// and all the `lexicon_engine_*` free functions declared in c_api.h.
// `#import` is used (rather than `#include`) for the built-in include-guard
// semantics provided by Clang's Objective-C preprocessor.
#import "../../cpp/include/lexicon/c_api.h"

#endif /* LEXICON_ENGINE_BRIDGING_HEADER_H */
