package com.zhou.captain.loopermonitordemo;

import android.os.Build;
import android.os.Looper;
import android.os.MessageQueue;
import android.util.Log;
import android.util.Printer;

import androidx.annotation.CallSuper;


import java.lang.reflect.Field;
import java.util.HashSet;

/**
 * @author zhouzheng
 */
public class LooperMonitor implements MessageQueue.IdleHandler {

    private static final HashSet<LooperDispatchListener> listeners = new HashSet<>();
    private static final String TAG = "Matrix.LooperMonitor";
    private static Printer printer;
    public static Printer testPrinter = null;

    public abstract static class LooperDispatchListener {

        boolean isHasDispatchStart = false;

        boolean isValid() {
            return false;
        }

        @CallSuper
        void dispatchStart() {
            this.isHasDispatchStart = true;
        }

        @CallSuper
        void dispatchEnd() {
            this.isHasDispatchStart = false;
        }
    }

//    private static final LooperMonitor monitor = new LooperMonitor();

     static {
           final LooperMonitor monitor = new LooperMonitor();
         Log.e("ANRTrace","dispatchStart");

     }

    private LooperMonitor() {
        resetPrinter();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Looper.getMainLooper().getQueue().addIdleHandler(this);
        } else {
            MessageQueue queue = reflectObject(Looper.getMainLooper(), "mQueue");
            queue.addIdleHandler(this);
        }
    }

    @Override
    public boolean queueIdle() {
        resetPrinter();
        return true;
    }


    private static void resetPrinter() {
        final Printer originPrinter = reflectObject(Looper.getMainLooper(), "mLogging");
        if (originPrinter == printer && null != printer) {
            return;
        }
        if (null != printer) {
            Log.w(TAG, "[resetPrinter] maybe looper printer was replace other!");
        }
        Looper.getMainLooper().setMessageLogging(printer = new Printer() {
            boolean isHasChecked = false;
            boolean isValid = false;

            @Override
            public void println(String x) {
                if (null != originPrinter) {
                    originPrinter.println(x);
                }

                if (!isHasChecked) {
                    isValid = x.charAt(0) == '>' || x.charAt(0) == '<';
                    isHasChecked = true;
                    if (!isValid) {
                        Log.e(TAG, "[println] Printer is inValid! x:%s"+ x);
                    }
                }

                if (isValid) {
                    dispatch(x.charAt(0) == '>');
                    if (null != testPrinter) {
                        testPrinter.println(x);
                    }
                }

            }
        });
    }

    public static void register(LooperDispatchListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public static void unregister(LooperDispatchListener listener) {
        if (null == listener) {
            return;
        }
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }


    private static void dispatch(boolean isBegin) {

        for (LooperDispatchListener listener : listeners) {
            if (listener.isValid()) {
                if (isBegin) {
                    if (!listener.isHasDispatchStart) {
                        listener.dispatchStart();
                    }
                } else {
                    if (listener.isHasDispatchStart) {
                        listener.dispatchEnd();
                    }
                }
            } else if (!isBegin && listener.isHasDispatchStart) {
                listener.dispatchEnd();
            }
        }

    }

    private static <T> T reflectObject(Object instance, String name) {
        try {
            Field field = instance.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return (T) field.get(instance);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }
        return null;
    }

}
