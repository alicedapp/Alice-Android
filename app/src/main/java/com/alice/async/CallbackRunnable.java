package com.alice.async;

/**
 * Created by sw on 2017/3/3.
 */

public abstract class CallbackRunnable<T> implements Runnable {

    private CallBacks mCallBacks;

    public void setCallBacks(CallBacks callBacks){
        mCallBacks = callBacks;
    }


    public interface CallBacks<T>{
        void call(T t);
    }

    @Override
    public void run() {
        final T t = handle();
        if(mCallBacks != null){
            if(isNeedMainThread()){
                MainHandler.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        mCallBacks.call(t);
                    }
                });
            }else{
                WorkThreadHandler.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        mCallBacks.call(t);
                    }
                });
            }

        }
    }

    public abstract T handle();

    public abstract boolean isNeedMainThread();


}
