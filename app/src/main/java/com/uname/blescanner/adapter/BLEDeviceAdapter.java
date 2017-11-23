package com.uname.blescanner.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.uname.blescanner.R;
import com.uname.blescanner.module.BLEDevice;
import com.uname.blescanner.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by apache on 4/20/2015.
 */
public class BLEDeviceAdapter extends BaseAdapter {

    private static final String TAG = BLEDeviceAdapter.class.getSimpleName();

    private Context _context;

    private int _resource;
    private List<BLEDevice> _bleDeviceList;
    private BLEDevice _curDevice;

    public BLEDeviceAdapter(Context context, int resource) {
        _context = context;
        _resource = resource;
        _bleDeviceList = new ArrayList<BLEDevice>();
    }

    public BLEDevice getCurDevice() {
        return _curDevice;
    }

    public void setCurDevice(BLEDevice device) {
        _curDevice = device;
    }

    public void clear() {
        if(_bleDeviceList != null) {
            _bleDeviceList.clear();
        }
    }

    public void setCurDevice(int position) {
        if(position  < 0 || position >= _bleDeviceList.size()) {
            LogUtil.d(TAG, "position error");
            return;
        }
        _curDevice = _bleDeviceList.get(position);
    }

    public void addDevice(BluetoothDevice leDevice, int rssi) {
        BLEDevice device = findBLEDeviceByName(leDevice.getName());
        if(device == null) {
            _bleDeviceList.add(new BLEDevice(leDevice, rssi));
        } else {
            device.setRssi(rssi);
        }
    }

    public BluetoothDevice getDevice(final int position) {
        if(position >= 0 && position < _bleDeviceList.size()) {
            return _bleDeviceList.get(position).getLeDevice();
        }

        return null;
    }

    public BLEDevice findBLEDeviceByName(String name) {
        int i = 0;
        for(BLEDevice ldDevice : _bleDeviceList) {
            if(name != null && ldDevice.getName().equals(name)) {
                return _bleDeviceList.get(i);
            }
            i++;
        }

        return null;
    }

    @Override
    public int getCount() {
        return _bleDeviceList.size();
    }

    @Override
    public Object getItem(int position) {
        if(position > 0 && position < _bleDeviceList.size()) {
            return _bleDeviceList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;

        if(view == null) {
            view = LayoutInflater.from(_context).inflate(_resource, null);
            viewHolder = new ViewHolder();
            viewHolder._deviceName = (TextView) view.findViewById(R.id.ble_name);
            viewHolder._deviceAddress = (TextView) view.findViewById(R.id.ble_address);
            viewHolder._sigImage = (ImageView) view.findViewById(R.id.ble_sig_image);
            viewHolder._deviceRssi = (TextView) view.findViewById(R.id.ble_rssi);
            view.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        BLEDevice device = _bleDeviceList.get(i);
        String deviceName = device.getName();
        int rssi = device.getRssi();
        int sigImageId;
        if(rssi > -40) {
            sigImageId = R.mipmap.sig_4;
        } else if (rssi > -60) {
            sigImageId = R.mipmap.sig_3;
        } else if (rssi > -80) {
            sigImageId = R.mipmap.sig_2;
        } else {
            sigImageId = R.mipmap.sig_1;
        }

        viewHolder._deviceRssi.setText(rssi + " db");
        if(deviceName == null) {
            deviceName = _context.getResources().getString(R.string.unknown_device);
        }
        viewHolder._deviceName.setText(deviceName);
        viewHolder._deviceAddress.setText(device.getAddress());
        viewHolder._sigImage.setImageResource(sigImageId);

        return view;
    }

    static class ViewHolder {
        TextView _deviceName;
        TextView _deviceAddress;
        ImageView _sigImage;
        TextView _deviceRssi;
    }
}
