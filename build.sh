#!/bin/bash

# Exit on error
set -e

# Use mock google services
cp mock-google-services.json app/google-services.json
./gradlew clean build

# Build
./gradlew clean build
