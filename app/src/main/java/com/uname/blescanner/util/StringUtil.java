package com.uname.blescanner.util;

/**
 * Created by apache on 5/4/2015.
 */
public class StringUtil {
	
	public final static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    
    public static String bytesToHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}
