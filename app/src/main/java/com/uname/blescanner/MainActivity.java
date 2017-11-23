package com.uname.blescanner;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.uname.blescanner.adapter.BLEDeviceAdapter;
import com.uname.blescanner.util.LogUtil;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static int REQUEST_ENABLE_BT = 1;
    private boolean mExitFlag = false;

    private ListView mBleListView;
    private BLEDeviceAdapter mBleDeviceAdapter;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BLEHelper.getInstance().setOnBleListener(new BLEHelper.OnBleListener() {

            @Override
            public void onScanResult(BluetoothDevice device, int rssi) {
                mBleDeviceAdapter.addDevice(device, rssi);
                mBleDeviceAdapter.notifyDataSetChanged();
            }

            @Override
            public void onConnectResult(boolean result, String address) {
                if(result) {
                    LogUtil.i(TAG, "connect success");
                   // SettingsActivity.actionStart(MainActivity.this, SettingsActivity.class);

                } else {
                    Toast.makeText(MainActivity.this, R.string.ble_connect_failed, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onDisconnected() {

            }

            @Override
            public void onDataReceived(final String address, final byte[] data) {
                BLEHelper.getInstance().dispatchBuff(address, data);
            }
        });

        iniView();
    }

    private void iniView() {
        mBleDeviceAdapter = new BLEDeviceAdapter(this, R.layout.ble_device_item);
        mBleListView = (ListView) findViewById(R.id.ble_device_list);
        mBleListView.setAdapter(mBleDeviceAdapter);
        mBleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //BLEHelper.getInstance().btConnect(null, mBleDeviceAdapter.getDevice(position).getAddress());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!BLEHelper.getInstance().btIsBluetoothOn()) {
            BLEHelper.getInstance().btTurnOn(this, REQUEST_ENABLE_BT);
            return;
        }

        BLEHelper.getInstance().btStartScan();
    }

    @Override
    protected void onPause() {
        super.onPause();
        BLEHelper.getInstance().btStopScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_CANCELED) {
            finish();
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode != KeyEvent.KEYCODE_BACK) {
            return false;
        }

        if(mExitFlag) {
            finish();
            return false;
        }
        Toast.makeText(this, R.string.exit_tip, Toast.LENGTH_SHORT).show();
        mExitFlag = true;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mExitFlag = false;
            }
        }, 3000);

        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.d(TAG, "onDestroy");
        BLEHelper.getInstance().unRegisterBleBroadcastReceiver();
    }
}
