package com.uname.blescanner;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import com.uname.blescanner.ble.BLEConfig;
import com.uname.blescanner.ble.BLEService;
import com.uname.blescanner.util.LogUtil;
import com.uname.blescanner.util.StringUtil;

import java.util.UUID;

/**
 * Created by apache on 6/19/2015.
 */
public class BLEClient {

    private static final String TAG = BLEClient.class.getSimpleName();

    private static final String SERVICE_UUID = BLEConfig.SERVICE_UUID;
    private static final String SEND_UUID = BLEConfig.SEND_UUID;
    private static final String RECEIVE_UUID = BLEConfig.RECEIVE_UUID;
    private static final int MAX_SEND_INTERVAL = BLEConfig.MAX_SEND_INTERVAL;// 最大的数据发送间隔(ms)
    private static final int MAX_SEND_BUFF_SIZE = BLEConfig.MAX_SEND_BUFF_SIZE; // 一次发送的数据的最大长度(超过该长度后需要拆包发送)

    private BLEService mBLEService;
    private BluetoothGattCharacteristic mReceiveCharacteristic;
    private BluetoothGattCharacteristic mSendCharacteristic;

    private boolean mIsInit;
    private long mLastSentTime ;

    
    public BLEClient(String address, BLEService bleService) {
        mBLEService = bleService;
    }

    public boolean init() {
        if(mIsInit) {
            return true;
        }

        BluetoothGattService gattService = mBLEService.getGattService(UUID.fromString(SERVICE_UUID));
        if(gattService == null) {
            LogUtil.d(TAG, "gattService is null");
            return false;
        }

        mReceiveCharacteristic = gattService.getCharacteristic(UUID.fromString(RECEIVE_UUID));
        if(mReceiveCharacteristic == null) {
            LogUtil.e(TAG, "receive gattCharacteristic is null");
            return false;
        }
        mBLEService.setCharacteristicNotification(mReceiveCharacteristic, true);

        mSendCharacteristic = gattService.getCharacteristic(UUID.fromString(SEND_UUID));
        if(mSendCharacteristic == null) {
            LogUtil.e(TAG, "send gattCharacteristic is null");
            return false;
        }


        mIsInit = true;

        return true;
    }

    private synchronized int readSend(final byte[] buff) {
        long t = System.currentTimeMillis();
        long dt = t - mLastSentTime;
        if(dt < MAX_SEND_INTERVAL) {
            LogUtil.d(TAG, "#################### send interval too small, need to sleep for a while");
            LogUtil.d(TAG,  "_lastSentTime=" + mLastSentTime + ", currentTime=" + t + ", dt=" + dt);
            try {
                // 暂时直接在这里sleep吧，时间比较短，应该不会对主线程造成神马影响
                Thread.sleep(MAX_SEND_INTERVAL - dt);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        boolean ret =  mSendCharacteristic.setValue(buff);
        LogUtil.d(TAG, "SEND size = " + buff.length + ", BUFF: " + StringUtil.bytesToHexString(buff));
        LogUtil.d(TAG, "setValue result is " + ret);
        mBLEService.writeCharacteristic(mSendCharacteristic);
        mLastSentTime = System.currentTimeMillis();

        return buff.length;
    }

    public synchronized  int send(final byte[] buff) {
        if(buff == null) {
            return 0;
        }
        int sentSize = 0;
        if(buff.length > MAX_SEND_BUFF_SIZE) {
            LogUtil.w(TAG, "buff size too big, need split");
            int maxSecNum = buff.length / MAX_SEND_BUFF_SIZE;
            int leftSize = buff.length % MAX_SEND_BUFF_SIZE;
            byte[] tmpBuf = new byte[MAX_SEND_BUFF_SIZE];
            for(int i = 0; i < maxSecNum; i++) {
                System.arraycopy(buff, i * MAX_SEND_BUFF_SIZE, tmpBuf, 0, MAX_SEND_BUFF_SIZE);
                sentSize += readSend(tmpBuf);
            }
            if(leftSize > 0) {
                tmpBuf = new byte[leftSize];
                System.arraycopy(buff, maxSecNum * MAX_SEND_BUFF_SIZE , tmpBuf, 0, leftSize);
                sentSize += readSend(tmpBuf);
            }
            return buff.length;
        } else {
            sentSize += readSend(buff);
        }

        return sentSize;
    }

    public void processReceivedBuff(byte[] buff) {

    }

    public void close() {
    	mBLEService.disconnect();
        mBLEService.close();
    }
}
