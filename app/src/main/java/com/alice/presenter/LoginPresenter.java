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
    public final static int IMPORT = 0X12;

    private int PERMISSIONS;


    public LoginPresenter(Activity context, ILoginView view){
        super(context,view);
    }


    public void checkWallet() {
        PERMISSIONS = CREATE;
        if((PermissionUtils.CheckPermission(PermissionUtils.READ_EXTERNAL_STORAGE,mContext)
                &&PermissionUtils.CheckPermission(PermissionUtils.WRITE_EXTERNAL_STORAGE,mContext))){
            //if already create key,tip
            if(!TextUtils.isEmpty(Hawk.get(Constants.KEY_STORE_PATH))){
                String path = Hawk.get(Constants.KEY_STORE_PATH);
                File file = new File(path);
                if(file.exists()){
                    mView.showCreateDialog();
                    return;
                }
            }
            createWallet();
        }else{
            PermissionUtils.verifyStoragePermissions(mContext);
        }
    }

    public void createWallet(){
        PERMISSIONS = CREATE;
        if((PermissionUtils.CheckPermission(PermissionUtils.READ_EXTERNAL_STORAGE,mContext)
                &&PermissionUtils.CheckPermission(PermissionUtils.WRITE_EXTERNAL_STORAGE,mContext))){
            manager.createWallet(new BaseListener<String>() {
                @Override
                public void OnSuccess(String memorizingWords) {
                    mView.showToast("Create wallet success,the memorizingWords is "+ memorizingWords);
                    mView.importSuccess();
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
                case IMPORT:
                    importWallet();
                    break;
            }
        }
    }


    public void importWallet() {
        PERMISSIONS = IMPORT;
        if((PermissionUtils.CheckPermission(PermissionUtils.READ_EXTERNAL_STORAGE,mContext)
                &&PermissionUtils.CheckPermission(PermissionUtils.WRITE_EXTERNAL_STORAGE,mContext))){
            manager.importWallet(new BaseListener<Credentials>() {
                @Override
                public void OnSuccess(Credentials credentials) {
                    mView.importSuccess();
                    mView.showToast("Import wallet success,the address is "+credentials.getAddress());
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
}
