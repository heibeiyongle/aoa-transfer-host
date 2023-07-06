package com.zhe.req.reghelper.application;

import android.app.Application;
import android.os.Handler;
import android.util.Log;

import com.zhe.req.reghelper.network.HttpManagerNew;


/**
 * Created by zhe on 15/12/28.
 */
public class AppApplication extends Application {
    private static final String TAG = "AppApplication";
    public static Handler handler;
    public static long MainThreadId;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate...");
        handler = new Handler();
        MainThreadId = android.os.Process.myTid();
        HttpManagerNew.init();
    }


}

