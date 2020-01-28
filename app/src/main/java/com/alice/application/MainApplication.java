package com.alice.application;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;

import com.alice.BuildConfig;
import com.alice.CustomToastPackage;
import com.alice.R;
import com.alice.async.MainHandler;
import com.alice.config.IConfig;
import com.facebook.react.ReactApplication;
import com.horcrux.svg.SvgPackage;
import com.mkuczera.RNReactNativeHapticFeedbackPackage;
import com.orhanobut.hawk.Hawk;
import com.swmansion.gesturehandler.react.RNGestureHandlerPackage;
import com.microsoft.codepush.react.CodePush;
import org.reactnative.camera.RNCameraPackage;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.facebook.soloader.SoLoader;

import  io.invertase.firebase.messaging.RNFirebaseMessagingPackage;
import com.masteratul.exceptionhandler.ReactNativeExceptionHandlerPackage;

import java.util.Arrays;
import java.util.List;

import io.invertase.firebase.RNFirebasePackage;

public class MainApplication extends Application implements ReactApplication {

  private final ReactNativeHost mReactNativeHost = new ReactNativeHost(this) {

        @Override
        protected String getJSBundleFile() {
        return CodePush.getJSBundleFile();
        }
    
    @Override
    public boolean getUseDeveloperSupport() {
      return BuildConfig.DEBUG;
    }

    @Override
    protected List<ReactPackage> getPackages() {
      return Arrays.<ReactPackage>asList(
              new MainReactPackage(),
              new SvgPackage(),
              new RNReactNativeHapticFeedbackPackage(),
              new RNGestureHandlerPackage(),
              new CodePush(getResources().getString(R.string.reactNativeCodePush_androidDeploymentKey), getApplicationContext(), BuildConfig.DEBUG),
              new RNCameraPackage(),
              new RNFirebasePackage(),
              new RNFirebaseMessagingPackage(),
              new ReactNativeExceptionHandlerPackage(),
              new CustomToastPackage()
      );
    }

    @Override
    protected String getJSMainModuleName() {
      return "index";
    }
  };

  @Override
  public ReactNativeHost getReactNativeHost() {
    return mReactNativeHost;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    SoLoader.init(this, /* native exopackage */ false);
    init();
  }


  private void init() {
    boolean isCoreProcess = false;
    List<ActivityManager.RunningAppProcessInfo> processInfos = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE)).getRunningAppProcesses();
    if (processInfos != null) {
      int pid = android.os.Process.myPid();
      for (ActivityManager.RunningAppProcessInfo info : processInfos) {
        if (info.pid == pid) {
          if (IConfig.CORE_PROCESS_NAME.equals(info.processName)) {
            isCoreProcess = true;
          }
          break;
        }
      }
    }
    //主进程
    if (isCoreProcess) {
      //init sp
      Hawk.init(this).build();
      MainHandler.init(this);
    }
  }
}
