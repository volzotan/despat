#!/bin/sh

set -e

cd Despat
./gradlew clean
# ./gradlew build -x lint
./gradlew assemble
scp app/build/outputs/apk/release/app-release-unsigned.apk grinzold:/var/www/grinzold/despat/despat.apk
echo "done"