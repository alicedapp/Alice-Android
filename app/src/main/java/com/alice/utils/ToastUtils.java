package com.alice.utils;

import android.widget.Toast;

import com.alice.application.MainApplication;

/**
 * @Description: java类作用描述
 * @Author: zhanghaoran3
 * @CreateDate: 2020/1/29 21:09
 */
public class ToastUtils {
    public static void makeText(String message){
        Toast.makeText(MainApplication.getAppContext(),message,Toast.LENGTH_LONG).show();
    }

    public static void makeText(int redId){
        Toast.makeText(MainApplication.getAppContext(),MainApplication.getAppContext().getString(redId),Toast.LENGTH_LONG).show();
    }
}
