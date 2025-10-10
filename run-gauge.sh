#!/bin/bash

# Build the project first
./gradlew testClasses

# Get the test runtime classpath from Gradle
CLASSPATH=$(./gradlew -q printTestClasspath)

# Set Gauge environment variables and run
export gauge_custom_compile_dir="build/classes/java/main:build/classes/java/test"
export gauge_custom_classpath="$CLASSPATH"

# Run Gauge with the specs
gauge run specs "$@"