package com.uname.blescanner.ble;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

/**
 * Created by apache on 4/21/2015.
 */

public class BLEBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = BLEBroadcastReceiver.class.getSimpleName();

    public static final int MSG_GATT_CONNECTED = 1;
    public static final int MSG_GATT_DISCONNECTED = 2;
    public static final int MSG_GATT_SERVICES_DISCOVERED = 3;
    public static final int MSG_DATA_AVAILABLE = 4;

    private Handler mHandler;

    public BLEBroadcastReceiver(Handler handler) {
        mHandler = handler;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        String address = intent.getStringExtra(BLEService.DEVICE_ADDRESS);

        if (BLEService.ACTION_GATT_CONNECTED.equals(action)) {
            mHandler.obtainMessage(MSG_GATT_CONNECTED, address).sendToTarget();
        }
        else if (BLEService.ACTION_GATT_DISCONNECTED.equals(action)) {
            mHandler.obtainMessage(MSG_GATT_DISCONNECTED, address).sendToTarget();
        }
        else if (BLEService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
            mHandler.obtainMessage(MSG_GATT_SERVICES_DISCOVERED, address).sendToTarget();
        }
        else if (BLEService.ACTION_DATA_AVAILABLE.equals(action)) {
            mHandler.obtainMessage(MSG_DATA_AVAILABLE, intent).sendToTarget();
        }
    }
}
