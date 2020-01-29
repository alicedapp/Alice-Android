package com.alice.presenter;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.alice.async.BaseListener;
import com.alice.config.Constants;
import com.alice.presenter.base.BasePresenter;
import com.alice.utils.PermissionUtils;
import com.alice.view.ILoginView;
import com.orhanobut.hawk.Hawk;


import org.web3j.crypto.Credentials;

import java.io.File;


public class LoginPresenter extends BasePresenter<ILoginView> {

    public final static int CREATE = 0X11;

    private int PERMISSIONS;


    public LoginPresenter(Activity context, ILoginView view){
        super(context,view);
    }


    public void createWallet(){
        PERMISSIONS = CREATE;
        if((PermissionUtils.CheckPermission(PermissionUtils.READ_EXTERNAL_STORAGE,mContext)
                &&PermissionUtils.CheckPermission(PermissionUtils.WRITE_EXTERNAL_STORAGE,mContext))){
            manager.createWallet(new BaseListener<String>() {
                @Override
                public void OnSuccess(String memorizingWords) {
                    mView.showToast("Create wallet success,the memorizingWords is "+ memorizingWords);
                    if(TextUtils.isEmpty(memorizingWords)){
                        mView.createFailed("create failed");
                        return;
                    }
                    manager.importWallet(memorizingWords, new BaseListener<Credentials>() {
                        @Override
                        public void OnSuccess(Credentials credentials) {
                            mView.createSuccess();
                        }

                        @Override
                        public void OnFailed(Throwable e) {
                            mView.createFailed(e.getMessage());
                        }
                    });
                }

                @Override
                public void OnFailed(Throwable e) {
                    mView.showToast(e.toString());
                }
            });
        }else{
            PermissionUtils.verifyStoragePermissions(mContext);
        }
    }



    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ){
            switch (PERMISSIONS){
                case CREATE:
                    createWallet();
                    break;
            }
        }
    }

}
