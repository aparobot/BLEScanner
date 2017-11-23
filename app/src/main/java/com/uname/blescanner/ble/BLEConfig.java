package com.uname.blescanner.ble;

/**
 * Created by Administrator on 2015/8/3.
 */
public class BLEConfig {
    public final static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public final static String SERVICE_UUID = "0000ff12-0000-1000-8000-00805f9b34fb";
    public final static String SEND_UUID = "0000ff01-0000-1000-8000-00805f9b34fb";
    public final static String RECEIVE_UUID = "0000ff02-0000-1000-8000-00805f9b34fb";

    public final static int MAX_SEND_INTERVAL = 2; //ms
    public final static int MAX_SEND_BUFF_SIZE = 20; //byte

}
