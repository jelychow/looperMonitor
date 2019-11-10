package com.zhou.captain.loopermonitordemo;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

/**
 * Created by zhouzheng on 2019-11-10.
 * company TaoMu Tech
 */
public class ANRTrace {
    public static AnrHandleTask anrHandleTask;
    public static  Handler handler;
    public static HandlerThread handlerThread;
    public static void init() {
        handlerThread =  CustomeHandlerThread.getDefaultHandlerThread();

        handler = new Handler(handlerThread.getLooper());
        LooperMonitor.register(new LooperMonitor.LooperDispatchListener() {
            @Override
            public boolean isValid() {
                return true;
            }

            @Override
            public void dispatchStart() {
                super.dispatchStart();
                anrHandleTask = new AnrHandleTask();
                handler.postDelayed(anrHandleTask,5000);
//                Log.e("ANRTrace","dispatchStart");
            }

            @Override
            public void dispatchEnd() {
                super.dispatchEnd();
                handler.removeCallbacks(anrHandleTask);
//                Log.e("ANRTrace","dispatchEnd");

            }

        });
    }

   static class AnrHandleTask implements Runnable {

        @Override
        public void run() {
            Log.e("ANRTrace","ANR happens");
        }
    }
}
