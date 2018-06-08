#!/bin/sh

cd Despat
./gradlew clean
# ./gradlew build -x lint
./gradlew assemble
scp app/build/outputs/apk/release/app-release-unsigned.apk grinzold.de/var/www/grinzold/despat/
echo "done"