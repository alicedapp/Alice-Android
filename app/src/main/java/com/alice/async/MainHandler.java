package com.alice.async;

import android.content.Context;
import android.os.Handler;

/**
 * 避免多个地方都创建一个Handler
 * 但是有可能会造成消息阻塞
 * @author mengshu
 *
 */
public class MainHandler extends Handler {
	
	private static MainHandler sInstance;
	private Context mContext;
	
	public static void init(Context context){
		sInstance = new MainHandler(context);
	}
	
	public static MainHandler getInstance(){
		return sInstance;
	}
	
	private MainHandler(Context context){
		mContext = context;
	}

}
