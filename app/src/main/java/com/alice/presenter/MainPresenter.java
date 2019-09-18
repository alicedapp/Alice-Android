package com.alice.presenter;

import android.app.Activity;
import android.content.pm.PackageManager;

import com.alice.manager.Web3jManager;
import com.alice.presenter.base.BasePresenter;
import com.alice.utils.PermissionUtils;
import com.alice.view.IMainView;

import org.web3j.crypto.Credentials;

public class MainPresenter extends BasePresenter<IMainView> {


    public final static int CREATE = 0X11;
    public final static int IMPORT = 0X12;

    private int PERMISSIONS;


    private Web3jManager manager;

    public MainPresenter(Activity context, IMainView view){
        super(context,view);
        manager = Web3jManager.getInstance();
    }

    public void createWallet() {
        //检查读写权限
        PERMISSIONS = CREATE;
        if((PermissionUtils.CheckPermission(PermissionUtils.READ_EXTERNAL_STORAGE,mContext)
                &&PermissionUtils.CheckPermission(PermissionUtils.WRITE_EXTERNAL_STORAGE,mContext))){
            manager.createWallet(new Web3jManager.OnCreateWalletListener() {
                @Override
                public void OnSuccess(Credentials credentials) {
                    mView.showToast("create success"+credentials.getAddress());
                }

                @Override
                public void OnFailed(Exception e) {
                    mView.showToast(e.toString());
                }
            });
        }else{
            PermissionUtils.verifyStoragePermissions(mContext);
        }
    }

    public void importWallet() {
        //检查读写权限
        PERMISSIONS = IMPORT;
        if((PermissionUtils.CheckPermission(PermissionUtils.READ_EXTERNAL_STORAGE,mContext)
                &&PermissionUtils.CheckPermission(PermissionUtils.WRITE_EXTERNAL_STORAGE,mContext))){
            manager.importWallet();

        }else{
            PermissionUtils.verifyStoragePermissions(mContext);
        }
    }

    public void checkBalances() {

    }

    public void transfer(String address) {

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
}
