package com.uname.blescanner.module;

import android.bluetooth.BluetoothDevice;

/**
 * Created by apache on 4/20/2015.
 */
public class BLEDevice {

    private final static int DEFAULT_IMAGE_ID = 0;

    private BluetoothDevice _leDevice;
    private int _imageId;
    private int _rssi;

    public BLEDevice(BluetoothDevice leDevice, int imageId, int rssi) {
        _leDevice = leDevice;
        _imageId = imageId;
        _rssi = rssi;
    }

    public BLEDevice(BluetoothDevice leDevice, int rssi) {
        this(leDevice, DEFAULT_IMAGE_ID, rssi);
    }

    public String getName() {
        String name = _leDevice.getName();
        if (name == null) {
            name = "UNKNOWN NAME";
        }
        return name;
    }

    public String getAddress() {
        return _leDevice.getAddress();
    }

    public int getImageId() {
        return _imageId;
    }

    public void setRssi(int rssi) {
        _rssi = rssi;
    }

    public int getRssi() {
        return _rssi;
    }

    public BluetoothDevice getLeDevice() {
        return _leDevice;
    }
}
