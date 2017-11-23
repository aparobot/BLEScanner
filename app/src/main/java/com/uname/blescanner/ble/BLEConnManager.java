package com.uname.blescanner.ble;


import android.content.Context;

import com.uname.blescanner.BLEClient;
import com.uname.blescanner.util.LogUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Created by apache on 6/19/2015.
 */
public class BLEConnManager {

    private static final String TAG = BLEConnManager.class.getSimpleName() ;

    private static BLEConnManager _instance;

    private Map<String, BLEConnHolder> _bleMap;

    private BLEConnManager() {
        _bleMap = new HashMap<String, BLEConnHolder>();
    }

    public static BLEConnManager getInstance() {
        if(_instance == null) {
            synchronized (BLEConnManager.class) {
                _instance = new BLEConnManager();
            }
        }
        return _instance;
    }

    public boolean connect(Context context, final String address) {
        if(address == null) {
            LogUtil.e(TAG, "address is null");
            return false;
        }

        BLEService _bleService = new BLEService(context, address);
        if(!_bleService.initialize()) {
            LogUtil.e(TAG, "Unable to initialize Bluetooth");
            return false;
        }

        boolean ret = _bleService.connect(address);
        if(ret) {
            _bleMap.put(address, new BLEConnHolder(_bleService));
            LogUtil.d(TAG, "connect<" + address + ">success");
        }

        return true;//initLeClient(address);
    }
    
    /**
     * 
     * @param address
     */
    public void disconnect(final String address) {
    	if("".equals(address)) {
    		LogUtil.d(TAG, "***** disconnect all ***** " + _bleMap.size());
    		Iterator<Entry<String, BLEConnHolder>> iter = _bleMap.entrySet().iterator();
    		while(iter.hasNext()) {
    			Entry<String, BLEConnHolder> entry =  iter.next();
    			if(entry.getValue() != null) {
    				LogUtil.d(TAG, "DISCONNECT");
    				BLEClient client = entry.getValue().bleClient;
                    BLEService bleService = entry.getValue().bleService;
    				if(client != null) {
    					client.close();
    				} else {
    					LogUtil.d(TAG, "Client not inited yet");
                        if(bleService != null) {
                            bleService.disconnect();
                            _bleMap.remove(address);
                        }
    				}
    			}
    		}
    		_bleMap.clear();
    		
    	} else {
	    	BLEClient client = getLeClient(address);
            BLEService bleService = getLeService(address);
	    	if(client != null) {
	    		client.close();
	    		_bleMap.remove(address);
	    	} else {
	    		LogUtil.e(TAG, "Error in disconnect: no such device( " + address + " )");
                if(bleService != null) {
                    bleService.disconnect();
                    _bleMap.remove(address);
                }
	    	}
    	}
    }

    public BLEClient getLeClient(final String address) {
        if(address == null) {
            LogUtil.e(TAG, "address is null");
            return null;
        }
        LogUtil.d(TAG, "keySet size: " + _bleMap.keySet().size());
        BLEConnHolder ci = _bleMap.get(address);
        if(ci == null) {
            LogUtil.e(TAG, "address not in map");
            return null;
        }

        LogUtil.d(TAG, "===============: " + ci.bleClient);
        return ci.bleClient;
    }

    public BLEService getLeService(final String address) {
        if(address == null) {
            LogUtil.e(TAG, "address is null");
            return null;
        }
        BLEConnHolder ci = _bleMap.get(address);
        if(ci == null) {
            LogUtil.e(TAG, "address not in map");
            return null;
        }

        return ci.bleService;
    }
    
    public boolean initLeClient(final String address) {
        if(address == null) {
            LogUtil.e(TAG, "address is null");
            return false;
        }

        BLEConnHolder ci = _bleMap.get(address);
        if(ci == null) {
            LogUtil.e(TAG, "address not in map");
            return false;
        }
        if(ci.bleClient != null) {
        	LogUtil.d(TAG, "has bleClient, just return it");
        	return true;
        }
        
        LogUtil.d(TAG, "bleClient not inited, do this...");
        ci.bleClient = new BLEClient(address, ci.bleService);
        if(!ci.bleClient.init()) {
            LogUtil.e(TAG, "client connect failed");
            ci.bleClient = null;
            return false;
        } else {
            LogUtil.i(TAG, "client connect success");
        }

        return true;
    }

    public int getConnBLECount() {
        return _bleMap.size();
    }
}
