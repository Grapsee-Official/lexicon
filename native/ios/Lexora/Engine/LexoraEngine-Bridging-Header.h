//  LexoraEngine-Bridging-Header.h
//  Import this in the Xcode project's "Objective-C Bridging Header" build
//  setting (SWIFT_OBJC_BRIDGING_HEADER = Lexora/Engine/LexoraEngine-Bridging-Header.h).
//  This exposes the C ABI to Swift.
//
//  Path resolution
//  ---------------
//  The import path below is relative to the iOS project root
//  (/native/ios/Lexora/), NOT to this file's directory. From the project
//  root, `../../cpp/include/lexora/c_api.h` resolves to
//  `/native/cpp/include/lexora/c_api.h` — the canonical location of the
//  C ABI header shipped by the C++ engine.
//
//  For this relative path to resolve at compile time, the Xcode target's
//  HEADER_SEARCH_PATHS must include:
//      $(SRCROOT)/Lexora
//  (Clang will then look up "../../cpp/include/lexora/c_api.h" starting from
//  /native/ios/Lexora/ and find /native/cpp/include/lexora/c_api.h.)
//
//  Alternatively set HEADER_SEARCH_PATHS to include
//  `$(SRCROOT)/../cpp/include` and replace the #import below with
//  `#import "lexora/c_api.h"` — both forms work.

#ifndef LEXORA_ENGINE_BRIDGING_HEADER_H
#define LEXORA_ENGINE_BRIDGING_HEADER_H

// Pull in the C ABI: LexoraEngineHandle, LexoraWordEntry, LexoraSearchResults,
// and all the `lexora_engine_*` free functions declared in c_api.h.
// `#import` is used (rather than `#include`) for the built-in include-guard
// semantics provided by Clang's Objective-C preprocessor.
#import "../../cpp/include/lexora/c_api.h"

#endif /* LEXORA_ENGINE_BRIDGING_HEADER_H */
