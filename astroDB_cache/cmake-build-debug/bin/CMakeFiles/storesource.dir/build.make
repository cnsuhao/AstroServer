# CMAKE generated file: DO NOT EDIT!
# Generated by "Unix Makefiles" Generator, CMake Version 3.6

# Delete rule output on recipe failure.
.DELETE_ON_ERROR:


#=============================================================================
# Special targets provided by cmake.

# Disable implicit rules so canonical targets will work.
.SUFFIXES:


# Remove some rules from gmake that .SUFFIXES does not remove.
SUFFIXES =

.SUFFIXES: .hpux_make_needs_suffix_list


# Suppress display of executed commands.
$(VERBOSE).SILENT:


# A target that is always out of date.
cmake_force:

.PHONY : cmake_force

#=============================================================================
# Set environment variables for the build.

# The shell in which to execute make rules.
SHELL = /bin/sh

# The CMake executable.
CMAKE_COMMAND = /home/hadoop/clion-2016.3.3/bin/cmake/bin/cmake

# The command to remove a file.
RM = /home/hadoop/clion-2016.3.3/bin/cmake/bin/cmake -E remove -f

# Escaping for special characters.
EQUALS = =

# The top-level source directory on which CMake was run.
CMAKE_SOURCE_DIR = /home/hadoop/CLionProjects/astroDB_cache

# The top-level build directory on which CMake was run.
CMAKE_BINARY_DIR = /home/hadoop/CLionProjects/astroDB_cache/cmake-build-debug

# Include any dependencies generated for this target.
include bin/CMakeFiles/storesource.dir/depend.make

# Include the progress variables for this target.
include bin/CMakeFiles/storesource.dir/progress.make

# Include the compile flags for this target's objects.
include bin/CMakeFiles/storesource.dir/flags.make

bin/CMakeFiles/storesource.dir/storeSourceData/storeIntoLocalDisk.cpp.o: bin/CMakeFiles/storesource.dir/flags.make
bin/CMakeFiles/storesource.dir/storeSourceData/storeIntoLocalDisk.cpp.o: ../src/storeSourceData/storeIntoLocalDisk.cpp
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir=/home/hadoop/CLionProjects/astroDB_cache/cmake-build-debug/CMakeFiles --progress-num=$(CMAKE_PROGRESS_1) "Building CXX object bin/CMakeFiles/storesource.dir/storeSourceData/storeIntoLocalDisk.cpp.o"
	cd /home/hadoop/CLionProjects/astroDB_cache/cmake-build-debug/bin && /usr/bin/c++   $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -o CMakeFiles/storesource.dir/storeSourceData/storeIntoLocalDisk.cpp.o -c /home/hadoop/CLionProjects/astroDB_cache/src/storeSourceData/storeIntoLocalDisk.cpp

bin/CMakeFiles/storesource.dir/storeSourceData/storeIntoLocalDisk.cpp.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing CXX source to CMakeFiles/storesource.dir/storeSourceData/storeIntoLocalDisk.cpp.i"
	cd /home/hadoop/CLionProjects/astroDB_cache/cmake-build-debug/bin && /usr/bin/c++  $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -E /home/hadoop/CLionProjects/astroDB_cache/src/storeSourceData/storeIntoLocalDisk.cpp > CMakeFiles/storesource.dir/storeSourceData/storeIntoLocalDisk.cpp.i

bin/CMakeFiles/storesource.dir/storeSourceData/storeIntoLocalDisk.cpp.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling CXX source to assembly CMakeFiles/storesource.dir/storeSourceData/storeIntoLocalDisk.cpp.s"
	cd /home/hadoop/CLionProjects/astroDB_cache/cmake-build-debug/bin && /usr/bin/c++  $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -S /home/hadoop/CLionProjects/astroDB_cache/src/storeSourceData/storeIntoLocalDisk.cpp -o CMakeFiles/storesource.dir/storeSourceData/storeIntoLocalDisk.cpp.s

bin/CMakeFiles/storesource.dir/storeSourceData/storeIntoLocalDisk.cpp.o.requires:

.PHONY : bin/CMakeFiles/storesource.dir/storeSourceData/storeIntoLocalDisk.cpp.o.requires

bin/CMakeFiles/storesource.dir/storeSourceData/storeIntoLocalDisk.cpp.o.provides: bin/CMakeFiles/storesource.dir/storeSourceData/storeIntoLocalDisk.cpp.o.requires
	$(MAKE) -f bin/CMakeFiles/storesource.dir/build.make bin/CMakeFiles/storesource.dir/storeSourceData/storeIntoLocalDisk.cpp.o.provides.build
.PHONY : bin/CMakeFiles/storesource.dir/storeSourceData/storeIntoLocalDisk.cpp.o.provides

bin/CMakeFiles/storesource.dir/storeSourceData/storeIntoLocalDisk.cpp.o.provides.build: bin/CMakeFiles/storesource.dir/storeSourceData/storeIntoLocalDisk.cpp.o


# Object files for target storesource
storesource_OBJECTS = \
"CMakeFiles/storesource.dir/storeSourceData/storeIntoLocalDisk.cpp.o"

# External object files for target storesource
storesource_EXTERNAL_OBJECTS =

lib/libstoresource.a: bin/CMakeFiles/storesource.dir/storeSourceData/storeIntoLocalDisk.cpp.o
lib/libstoresource.a: bin/CMakeFiles/storesource.dir/build.make
lib/libstoresource.a: bin/CMakeFiles/storesource.dir/link.txt
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --bold --progress-dir=/home/hadoop/CLionProjects/astroDB_cache/cmake-build-debug/CMakeFiles --progress-num=$(CMAKE_PROGRESS_2) "Linking CXX static library ../lib/libstoresource.a"
	cd /home/hadoop/CLionProjects/astroDB_cache/cmake-build-debug/bin && $(CMAKE_COMMAND) -P CMakeFiles/storesource.dir/cmake_clean_target.cmake
	cd /home/hadoop/CLionProjects/astroDB_cache/cmake-build-debug/bin && $(CMAKE_COMMAND) -E cmake_link_script CMakeFiles/storesource.dir/link.txt --verbose=$(VERBOSE)

# Rule to build all files generated by this target.
bin/CMakeFiles/storesource.dir/build: lib/libstoresource.a

.PHONY : bin/CMakeFiles/storesource.dir/build

bin/CMakeFiles/storesource.dir/requires: bin/CMakeFiles/storesource.dir/storeSourceData/storeIntoLocalDisk.cpp.o.requires

.PHONY : bin/CMakeFiles/storesource.dir/requires

bin/CMakeFiles/storesource.dir/clean:
	cd /home/hadoop/CLionProjects/astroDB_cache/cmake-build-debug/bin && $(CMAKE_COMMAND) -P CMakeFiles/storesource.dir/cmake_clean.cmake
.PHONY : bin/CMakeFiles/storesource.dir/clean

bin/CMakeFiles/storesource.dir/depend:
	cd /home/hadoop/CLionProjects/astroDB_cache/cmake-build-debug && $(CMAKE_COMMAND) -E cmake_depends "Unix Makefiles" /home/hadoop/CLionProjects/astroDB_cache /home/hadoop/CLionProjects/astroDB_cache/src /home/hadoop/CLionProjects/astroDB_cache/cmake-build-debug /home/hadoop/CLionProjects/astroDB_cache/cmake-build-debug/bin /home/hadoop/CLionProjects/astroDB_cache/cmake-build-debug/bin/CMakeFiles/storesource.dir/DependInfo.cmake --color=$(COLOR)
.PHONY : bin/CMakeFiles/storesource.dir/depend

