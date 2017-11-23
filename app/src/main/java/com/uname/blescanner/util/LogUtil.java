package com.uname.blescanner.util;

import android.util.Log;

/**
 * Created by apache on 4/23/2015.
 */
public class LogUtil {

    public static final int VERBOSE = 1;
    public static final int DEBUG = 2;
    public static final int INFO = 3;
    public static final int WARN = 4;
    public static final int ERROR = 5;
    public static final int NOTHING = 6;

    public static int _level = VERBOSE;

    public static void setLevel(int level) {
        if(_level >= VERBOSE && _level <= NOTHING) {
            _level = level;
        }
    }

    public static void v(String tag, String msg) {
        if(_level <= VERBOSE) {
            Log.v(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if(_level <= DEBUG) {
            Log.d(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if(_level <= INFO) {
            Log.i(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if(_level <= WARN) {
            Log.w(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if(_level <= ERROR) {
            Log.e(tag, msg);
        }
    }
}
