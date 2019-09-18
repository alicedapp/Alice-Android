package com.alice.presenter;

import android.app.Activity;
import android.content.pm.PackageManager;

import com.alice.async.BaseListener;
import com.alice.manager.Web3jManager;
import com.alice.presenter.base.BasePresenter;
import com.alice.utils.PermissionUtils;
import com.alice.view.IMainView;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.utils.Convert;

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
        PERMISSIONS = CREATE;
        if((PermissionUtils.CheckPermission(PermissionUtils.READ_EXTERNAL_STORAGE,mContext)
                &&PermissionUtils.CheckPermission(PermissionUtils.WRITE_EXTERNAL_STORAGE,mContext))){
            manager.createWallet(new BaseListener<String>() {
                @Override
                public void OnSuccess(String memorizingWords) {
                    mView.showToast("Create wallet success,the memorizingWords is "+ memorizingWords);
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

    public void importWallet() {
        PERMISSIONS = IMPORT;
        if((PermissionUtils.CheckPermission(PermissionUtils.READ_EXTERNAL_STORAGE,mContext)
                &&PermissionUtils.CheckPermission(PermissionUtils.WRITE_EXTERNAL_STORAGE,mContext))){
            manager.importWallet(new BaseListener<Credentials>() {
                @Override
                public void OnSuccess(Credentials credentials) {
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

    public void checkBalances() {
        manager.checkBalances(new BaseListener<EthGetBalance>(){
            @Override
            public void OnSuccess(EthGetBalance balance) {
                String blanceETH = Convert.fromWei(balance.getBalance().toString(), Convert.Unit.ETHER).toPlainString().concat("ether");
                mView.showToast("Get Balance Success,Balance is " + blanceETH);
            }

            @Override
            public void OnFailed(Throwable e) {
                mView.showToast("Get Balance Failed! exception is" + e.toString());
            }
        });
    }

    public void transfer(String address) {

    }

    @Override
    public void onDestroy() {
        manager.clear();
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
