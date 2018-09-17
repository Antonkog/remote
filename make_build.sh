#!/bin/sh
rm -rf ./build/*;
chmod a+x ./gradlew;
./gradlew clean;
fastlane beta
