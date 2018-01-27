#!/bin/sh

ADB=/usr/local/bin/adb
PKG=de.volzo.despat
PATH=/data/data/$PKG/databases/despat-database
OUT=despat.sqlite

echo "\n"
echo " > DELETE"
echo "\n"

$ADB shell "run-as $PKG chmod 666 $PATH"
$ADB exec-out run-as $PKG rm $PATH