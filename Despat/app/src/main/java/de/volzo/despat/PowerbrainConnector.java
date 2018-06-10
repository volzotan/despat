package de.volzo.despat;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

public class PowerbrainConnector {

    private static final String TAG = MainActivity.class.getSimpleName();
    public final String ACTION_USB_PERMISSION = "de.volzo.despat.USB_PERMISSION";

    UsbManager usbManager;
    UsbDevice device;
    UsbSerialDevice serialPort;
    UsbDeviceConnection connection;
    Context context;

    public PowerbrainConnector(Context context) {
        this.context = context;
        initialize();
    }

    public void initialize() {
        usbManager = (UsbManager) context.getSystemService(context.USB_SERVICE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        context.registerReceiver(broadcastReceiver, filter);
    }

    public void disconnect() {
        try {
            context.unregisterReceiver(broadcastReceiver);
        } catch (IllegalArgumentException iae) {
            Log.w(TAG, "trying to unregister not registered receiver");
        }
    }

    UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() { //Defining a Callback which triggers whenever data is read.
        @Override
        public void onReceivedData(byte[] arg0) {
            String data = null;
            try {
                data = new String(arg0, "UTF-8");
                data.concat("/n");
                Log.d(TAG, "USB DATA RECEIVED: " + data);

                parse(data);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    };

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { //Broadcast Receiver to automatically start and stop the Serial connection.
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted =
                        intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) {
                    connection = usbManager.openDevice(device);
                    serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                    if (serialPort != null) {
                        if (serialPort.open()) { //Set Serial Connection Parameters.
                            serialPort.setBaudRate(9600);
                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                            serialPort.read(mCallback); //
                            Log.d(TAG, "Serial Connection Opened!");

                        } else {
                            Log.d(TAG, "PORT NOT OPEN");
                        }
                    } else {
                        Log.d(TAG, "PORT IS NULL");
                    }
                } else {
                    Log.d(TAG, "PERM NOT GRANTED");
                }
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                // TODO
                Log.i(TAG, "USB device attached");
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                // TODO: pause update service
                Log.i(TAG, "USB device detached");
            }
        };
    };


    public boolean connect() {
        boolean success = false;

        HashMap usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            boolean keep = true;
            for (Object entry : usbDevices.values()) {
                device = (UsbDevice) entry;
                int deviceVID = device.getVendorId();
                Log.d(TAG, "USB device found. Vender ID: " + deviceVID);

                if (deviceVID == 0x2341) { //Arduino Vendor ID
                    PendingIntent pi = PendingIntent.getBroadcast(this.context, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    usbManager.requestPermission(device, pi);

                    success = false;
                    keep = false;
                } else {
                    connection = null;
                    device = null;
                }

                if (!keep)
                    break;
            }

        }

        return success;
    }

    public void send(String string) {
        serialPort.write(string.getBytes());
    }

    private void parse(String payload) {

    }

    public void startHandler() {

    }

    public void stopHandler() {

    }

    public int getBatteryState() {
        return -1; // TODO
    }
}
