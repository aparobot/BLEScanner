package com.uname.blescanner;

import android.app.Application;
import android.content.Context;

/**
 * Created by Administrator on 2015/10/23.
 */
public class MainApp extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this.getApplicationContext();
    }

    public static Context getContext() {
        return mContext;
    }

}
