package com.alice.presenter;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.alice.async.BaseListener;
import com.alice.async.WorkThreadHandler;
import com.alice.config.Constants;
import com.alice.manager.Web3jManager;
import com.alice.presenter.base.BasePresenter;
import com.alice.source.BaseDataSource;
import com.alice.utils.LogUtil;
import com.alice.utils.PermissionUtils;
import com.alice.view.IMainView;
import com.orhanobut.hawk.Hawk;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Convert;

import java.io.File;
import java.math.BigInteger;

public class MainPresenter extends BasePresenter<IMainView> {


    public final static int CREATE = 0X11;
    public final static int IMPORT = 0X12;

    private int PERMISSIONS;


    private Web3jManager manager;
    private BaseDataSource dataSource;

    public MainPresenter(Activity context, IMainView view){
        super(context,view);
        manager = Web3jManager.getInstance();
        dataSource = new BaseDataSource();
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
                    mView.showContent(credentials);
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
                String blanceETH = Convert.fromWei(balance.getBalance().toString(), Convert.Unit.ETHER).toPlainString().concat(" ether");
                mView.showToast("Get Balance Success,Balance is " + blanceETH);
            }

            @Override
            public void OnFailed(Throwable e) {
                mView.showToast("Get Balance Failed! exception is" + e.toString());
            }
        });
    }

    public void transfer(String address,String value) {
        manager.transfer(address,value,new BaseListener<TransactionReceipt>(){

            @Override
            public void OnSuccess(TransactionReceipt send) {
                String text = "Transaction complete:" + "trans hash=" + send.getTransactionHash() + "from :" + send.getFrom() + "to:" + send.getTo() + "gas used=" + send.getGasUsed() + "status: " + send.getStatus();
                LogUtil.d( text);
                mView.showToast(text);
            }

            @Override
            public void OnFailed(Throwable e) {
                mView.showToast("Transfer Failed! exception is" + e.toString());
            }
        });
    }

    public void smartContract() {
        WorkThreadHandler.getInstance().post(new Runnable() {
            @Override
            public void run() {
                String message = manager.getMessageName("0x2f21957c7147c3eE49235903D6471159a16c9ccd");
                mView.showToast(message);
            }
        });
    }

    @Override
    public void onDestroy() {
        manager.clear();
        dataSource.clear();
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

    public void signMessage() {
        manager.sign("0x48656c6c6f20576f726c64000000000000000000000000000000000000000000", new BaseListener<String>() {
            @Override
            public void OnSuccess(String s) {

            }

            @Override
            public void OnFailed(Throwable e) {

            }
        });
    }

    public void smartContractSet(String address, String functionName,String value, String[] params, BigInteger gasPrice) {
        WorkThreadHandler.getInstance().post(new Runnable() {
            @Override
            public void run() {
                mView.showToast(manager.setSmartContract(address,functionName,value,params,gasPrice));
            }
        });
    }
}
