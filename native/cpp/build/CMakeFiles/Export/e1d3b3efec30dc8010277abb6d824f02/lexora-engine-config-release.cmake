#----------------------------------------------------------------
# Generated CMake target import file for configuration "Release".
#----------------------------------------------------------------

# Commands may need to know the format version.
set(CMAKE_IMPORT_FILE_VERSION 1)

# Import target "lexora::lexora-engine" for configuration "Release"
set_property(TARGET lexora::lexora-engine APPEND PROPERTY IMPORTED_CONFIGURATIONS RELEASE)
set_target_properties(lexora::lexora-engine PROPERTIES
  IMPORTED_LOCATION_RELEASE "${_IMPORT_PREFIX}/lib/liblexora-engine.so.1.0.0"
  IMPORTED_SONAME_RELEASE "liblexora-engine.so.1"
  )

list(APPEND _cmake_import_check_targets lexora::lexora-engine )
list(APPEND _cmake_import_check_files_for_lexora::lexora-engine "${_IMPORT_PREFIX}/lib/liblexora-engine.so.1.0.0" )

# Commands beyond this point should not need to know the version.
set(CMAKE_IMPORT_FILE_VERSION)
