#!/bin/sh

ADB=/usr/local/bin/adb
PKG=de.volzo.despat
PATH=/data/data/$PKG/databases/despat-database
OUT=despat.sqlite

$ADB shell "run-as $PKG chmod 666 $PATH"
# $ADB pull $PATH .
$ADB exec-out run-as $PKG cat $PATH > $OUT
$ADB shell "run-as $PKG chmod 600 $PATH"