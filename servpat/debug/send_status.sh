http -a device1:foobar POST localhost:5000/status \
    deviceId=httpie                             \
    deviceName=httpieDevice1                    \
    timestamp=2017:10:10 \
    numberImages=123                            \
    freeSpaceInternal=100.01                    \
    freeSpaceExternal=-1                        \
    batteryInternal=75                          \
    batteryExternal=-1                          \
    stateCharging=1

    #zoltep.de/status    \