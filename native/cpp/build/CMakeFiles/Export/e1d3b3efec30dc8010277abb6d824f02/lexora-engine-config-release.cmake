#----------------------------------------------------------------
# Generated CMake target import file for configuration "Release".
#----------------------------------------------------------------

# Commands may need to know the format version.
set(CMAKE_IMPORT_FILE_VERSION 1)

# Import target "lexicon::lexicon-engine" for configuration "Release"
set_property(TARGET lexicon::lexicon-engine APPEND PROPERTY IMPORTED_CONFIGURATIONS RELEASE)
set_target_properties(lexicon::lexicon-engine PROPERTIES
  IMPORTED_LOCATION_RELEASE "${_IMPORT_PREFIX}/lib/liblexicon-engine.so.1.0.0"
  IMPORTED_SONAME_RELEASE "liblexicon-engine.so.1"
  )

list(APPEND _cmake_import_check_targets lexicon::lexicon-engine )
list(APPEND _cmake_import_check_files_for_lexicon::lexicon-engine "${_IMPORT_PREFIX}/lib/liblexicon-engine.so.1.0.0" )

# Commands beyond this point should not need to know the version.
set(CMAKE_IMPORT_FILE_VERSION)
