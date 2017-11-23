package com.uname.blescanner;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Administrator on 2015/12/7.
 */
public class BaseActivity extends AppCompatActivity {

    public static void actionStart(Context context, Class<?> cls) {
        Intent intent = new Intent(context, cls);
        context.startActivity(intent);
    }
}
