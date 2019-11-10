package com.zhou.captain.loopermonitordemo;

import android.app.Application;

/**
 * Created by zhouzheng on 2019-11-10.
 * company TaoMu Tech
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ANRTrace.init();
    }
}
