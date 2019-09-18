package com.alice.async;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

/**
 * 这个Handler扩展类主要用于在非UI线程执行一些耗时操作。 与MainHandler类区别 MainHandler是在主线程里执行一些操作。
 * 而WorkThreadHandler是在非UI线程执行一些耗时操作
 * 
 * @author zhaoyp
 */
public class WorkThreadHandler {

    private static WorkThreadHandler sInstance;
    private static HandlerThread mThread;
//    private Context mContext;
    private static Handler mHandler;


    public static void init(Context context) {
        if (null == sInstance) {
            synchronized (WorkThreadHandler.class){
                if(null == sInstance){
                    if(null == mThread){
                        mThread = new HandlerThread("backgroundtask");
                    }
                    mThread.start();
                    mHandler = new Handler(mThread.getLooper());
                    sInstance = new WorkThreadHandler();
                }
            }

        }

    }

    public void post(Runnable runnable){
        if(mHandler != null){
            mHandler.post(runnable);
        }
    }

    public void postDelayed(Runnable runnable,int mills){
        if(mHandler != null){
            mHandler.postDelayed(runnable,mills);
        }
    }

    public void quit(){
        if(null != mThread)
            mThread.quitSafely();

        sInstance = null;
        mThread = null;
        mHandler = null;

    }

    public static WorkThreadHandler getInstance() {
        if(null == sInstance){
            synchronized (WorkThreadHandler.class){
                if(null == sInstance){
                    if(null == mThread){
                        mThread = new HandlerThread("backgroundtask");
                    }
                    mThread.start();
                    mHandler = new Handler(mThread.getLooper());
                    sInstance = new WorkThreadHandler();
                }
            }
        }
        return sInstance;
    }

    private WorkThreadHandler() {
    }

    public void postWithCallback(CallbackRunnable runnable, CallbackRunnable.CallBacks callBacks){
        if(runnable == null){
            return;
        }
        runnable.setCallBacks(callBacks);
        mHandler.post(runnable);
    }
}