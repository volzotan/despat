# adb shell pm uninstall -k de.volzo.despat # keep the data dir
# adb shell pm uninstall -k de.volzo.despat
adb uninstall de.volzo.despat
adb install Despat/release/app-release.apk
adb shell am start -n de.volzo.despat/de.volzo.despat.userinterface.LaunchActivity