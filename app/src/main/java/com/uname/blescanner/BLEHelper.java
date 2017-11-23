package com.uname.blescanner;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;

import com.uname.blescanner.ble.BLEBroadcastReceiver;
import com.uname.blescanner.ble.BLEConnManager;
import com.uname.blescanner.ble.BLEService;
import com.uname.blescanner.util.LogUtil;
import com.uname.blescanner.util.StringUtil;

public class BLEHelper {

	protected static final String TAG = BLEHelper.class.getSimpleName();
	private static final int CONNECT_TIMEOUT = 5000;
	private static BLEHelper mInstance;
	private boolean mTimeoutFlag = false;
	private BluetoothManager _bluetoothManager;
	private BluetoothAdapter _bluetoothAdapter;
	private BLEBroadcastReceiver mGattReceiver;

	private OnBleListener mListener;
	private String mRecentAddress;

	private boolean _isConnecting = false;  // 是否正在连接BLE设备
//	private Map<String, Integer> _deviceInfo = new HashMap<String, Integer>();

	public interface OnBleListener {
		void onScanResult(final BluetoothDevice device, int rssi);
		void onConnectResult(boolean result, String address);
		void onDisconnected();
		void onDataReceived(final String address, final byte[] data);
	}

	private Runnable mConnectTimeoutRunnable = new Runnable() {
		@Override
		public void run() {
			mTimeoutFlag = true;
			LogUtil.d(TAG, "connect timeout:(");
			mListener.onConnectResult(false, "");
		}
	};

	private Handler mHandler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			if(mListener == null) {
				LogUtil.e(TAG, "BLEHelper is listener is null");
				return false;
			}

            String address = "";
			switch (msg.what) {
				case BLEBroadcastReceiver.MSG_GATT_SERVICES_DISCOVERED:
                    address = (String) msg.obj;
					boolean result = false;
					if(!mTimeoutFlag) {
						mHandler.removeCallbacks(mConnectTimeoutRunnable);
						result = BLEHelper.getInstance().realBtConnect(address);
					}
					mListener.onConnectResult(result, address);
					break;

				case BLEBroadcastReceiver.MSG_GATT_DISCONNECTED:
					mListener.onDisconnected();
					break;

				case BLEBroadcastReceiver.MSG_DATA_AVAILABLE:
                    address = ((Intent)msg.obj).getStringExtra(BLEService.DEVICE_ADDRESS);
                    byte[] buff = ((Intent)msg.obj).getByteArrayExtra(BLEService.EXTRA_DATA);
					mListener.onDataReceived(address, buff);
					break;

			}
			return false;
		}
	});

	protected BluetoothAdapter.LeScanCallback _leScanCallback =  new BluetoothAdapter.LeScanCallback() {
		@Override
	    public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
	        if(device == null) {
	            LogUtil.e(TAG, "device is null");
	            return;
	        }
	
//	        LogUtil.d(TAG, "device found: " + device.getName() + " # " + rssi );
	        String deviceName = device.getName();
	        String address = device.getAddress();
	        if(deviceName == null) {
	        	LogUtil.d(TAG, "ignore null name device");
	        	return;
	        }
			if(mListener != null) {
				mListener.onScanResult(device, rssi);
			}
	    }
	};
        
	private BLEHelper() {
		_bluetoothManager = (BluetoothManager) MainApp.getContext().getSystemService(Context.BLUETOOTH_SERVICE);
		assert _bluetoothManager != null;
		_bluetoothAdapter = _bluetoothManager.getAdapter();
		registerBleBroadcastReceiver();
	}

	public synchronized static BLEHelper getInstance() {
		if(mInstance == null) {
			mInstance = new BLEHelper();
		}
		return mInstance;
	}

	public void setOnBleListener(OnBleListener listener) {
		mListener = listener;
	}

	public boolean registerBleBroadcastReceiver() {
		if(mGattReceiver != null) {
			LogUtil.e(TAG, "please unregister first");
			return false;
		}
		final IntentFilter gattFilter = new IntentFilter();
		gattFilter.addAction(BLEService.ACTION_GATT_CONNECTED);
		gattFilter.addAction(BLEService.ACTION_GATT_DISCONNECTED);
		gattFilter.addAction(BLEService.ACTION_GATT_SERVICES_DISCOVERED);
		gattFilter.addAction(BLEService.ACTION_DATA_AVAILABLE);
		mGattReceiver = new BLEBroadcastReceiver(mHandler);
		MainApp.getContext().registerReceiver(mGattReceiver, gattFilter);

		return true;
	}

	public void unRegisterBleBroadcastReceiver() {
		if(mGattReceiver != null) {
			MainApp.getContext().unregisterReceiver(mGattReceiver);
			mGattReceiver = null;
		}
	}

	/**
	 * 检查手机是否支持蓝牙
	 * @return
	 */
	public boolean btIsBluetoothSupported() {
		return _bluetoothManager != null && _bluetoothAdapter  != null;
	}
	
	/**
	 * 检查蓝牙是否已打开
	 * @return
	 */
	public boolean btIsBluetoothOn() {
		return _bluetoothAdapter != null && _bluetoothAdapter.isEnabled();
	}
	/**
	 * 打开蓝牙
	 */
	public void btTurnOn(Activity activity, int requestCode) {
		Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		activity.startActivityForResult(enableBtIntent, requestCode);
	}
	
	/**
	 * 启动BLE蓝牙设备扫描
	 */
	@SuppressWarnings("deprecation")
	public void btStartScan() {
		if(_bluetoothAdapter != null) {
			_bluetoothAdapter.startLeScan(_leScanCallback); 
		}
	}
	
	/**
	 * 停止BLE设备扫描
	 * 在不需要扫描的时候记得调用该函数关闭扫描，因为真的很费电
	 */
	@SuppressWarnings("deprecation")
	public void btStopScan() {
		if(_bluetoothAdapter != null) {
			_bluetoothAdapter.stopLeScan(_leScanCallback);
//			_deviceInfo.clear();
		}
	}
	
	/**
	 * 连接到BLE设备
	 * @param address
	 * 如果address为""，则连接到最近的设备
	 */
	public boolean btConnect(Context context, final String address) {
		if("".equals(address)) {
			return false;
		}
		mRecentAddress = address;
		if(context == null) {
			context = MainApp.getContext();
		}
		btStopScan();
		mTimeoutFlag = false;
		mHandler.postDelayed(mConnectTimeoutRunnable, CONNECT_TIMEOUT);

		return btConnectToDevice(context, address);
	}
	
	/**
	 * 断开BLE设备
	 * @param address
	 * 如果address为""，则断开所有已连接的设备
	 */
	public void btDisconnect(String address) {
		if(address == null) {
			address = mRecentAddress;
		}
		BLEConnManager.getInstance().disconnect(address);
	}


	/**
	 *
	 * @param address
	 * @param buff
	 * @return
	 */
	public boolean btSend(String address, byte[] buff) {
		if(address == null) {
			address = mRecentAddress;
		}
		LogUtil.d(TAG, "send to<" + address + ">");
		BLEClient client = BLEConnManager.getInstance().getLeClient(address);
		if(client == null) {
			LogUtil.d(TAG, "btSend error: client is null");
			return false;
		}
		LogUtil.d(TAG, "---------------Packet BUFF IS------------: " + StringUtil.bytesToHexString(buff));
		boolean ret = client.send(buff) == buff.length;
		if(ret) {
			LogUtil.d(TAG, "SEND OK");
		} else {
			LogUtil.e(TAG,  "SEND FAILED");
		}
		return ret;
	}

	private boolean btConnectToDevice(Context context, final String address) {
		LogUtil.d(TAG, "------------------------------------------------");
		btStopScan();
		boolean result = BLEConnManager.getInstance().connect(context, address);
		if(!result) {
			// nothing
		} else {
			_isConnecting = true;
		}

		return result;
	}
	
	public boolean realBtConnect(String address) {
		_isConnecting = false;
		LogUtil.d(TAG, "real connect <<<<" + address + ">>>>>");
		return BLEConnManager.getInstance().initLeClient(address);
	}

	public void dispatchBuff(String address, byte[] buff) {
		LogUtil.d(TAG, "DATA From " + address + " --> " + StringUtil.bytesToHexString(buff));
		BLEClient client = BLEConnManager.getInstance().getLeClient(address);
		if(client == null) {
			LogUtil.e(TAG, "BLEClient on " + address + " is null");
			return;
		}
		client.processReceivedBuff(buff);
		LogUtil.d(TAG,  "BLEClient proc the data...");
	}

	public int getConnBLECount() {
		return BLEConnManager.getInstance().getConnBLECount();
	}
}
