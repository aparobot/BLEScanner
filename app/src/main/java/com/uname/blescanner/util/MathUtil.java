package com.uname.blescanner.util;

/**
 * Created by apache on 4/24/2015.
 */
public class MathUtil {
	
    public static byte crc8(final byte[] buff, int startPos, int endPos) {
        if(startPos < 0 || startPos > buff.length - 1 || endPos > buff.length - 1) {
            return 0;
        }
        char crc = 0;
        for(int i = startPos; i <= endPos; i++) {
            crc = (char) ((crc + (char)buff[i]) % 0xff);
        }
        return (byte)crc;
    }
}
