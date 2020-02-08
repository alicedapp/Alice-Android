package com.alice.presenter;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.alice.async.BaseListener;
import com.alice.async.WorkThreadHandler;
import com.alice.config.Constants;
import com.alice.manager.Web3jManager;
import com.alice.model.SmartContractMessage;
import com.alice.presenter.base.BasePresenter;
import com.alice.source.BaseDataSource;
import com.alice.utils.LogUtil;
import com.alice.utils.PermissionUtils;
import com.alice.view.IMainView;
import com.orhanobut.hawk.Hawk;

import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Convert;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class MainPresenter extends BasePresenter<IMainView> {


    public final static int CREATE = 0X11;
    public final static int IMPORT = 0X12;

    private int PERMISSIONS;


    private BaseDataSource dataSource;

    public MainPresenter(Activity context, IMainView view){
        super(context,view);
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
        List<Type> inputParameters = new ArrayList<>();
        List<TypeReference<?>> outputParameters = new ArrayList<>();
        TypeReference<Utf8String> typeReference = new TypeReference<Utf8String>() {};
        outputParameters.add(typeReference);
        manager.readFromContract("0x2f21957c7147c3eE49235903D6471159a16c9ccd", "getMessage", inputParameters, outputParameters, new BaseListener<String>() {
            @Override
            public void OnSuccess(String s) {
                mView.showToast(s);
            }

            @Override
            public void OnFailed(Throwable e) {
                mView.showToast(e.getMessage());
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

    public void signMessage(String message) {
        manager.sign(message, new BaseListener<String>() {
            @Override
            public void OnSuccess(String s) {
                mView.showToast(s);
                mView.onSignSuccess();
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

    public void loadSmartContractSet(String address, String function, String value, List<Type> params,List<TypeReference<?>> outputArgs) {
      manager.loadSmartContractSet(address,function,value,params,outputArgs,new BaseListener<SmartContractMessage>(){

          @Override
          public void OnSuccess(SmartContractMessage s) {
                mView.setBottomView(s);
          }

          @Override
          public void OnFailed(Throwable e) {
                mView.showToast(e.getMessage());
          }
      });
    }

    public void smartContractSet(SmartContractMessage smartContractMessage) {
        manager.setSmartContract(smartContractMessage,new BaseListener<String>(){

            @Override
            public void OnSuccess(String s) {
                mView.showToast(s);
            }

            @Override
            public void OnFailed(Throwable e) {
                mView.showToast(e.getMessage());
            }
        });
    }

    public void loadGasPrice(String address,String value) {
        manager.loadTransferInfo(address, value, new BaseListener<SmartContractMessage>() {
            @Override
            public void OnSuccess(SmartContractMessage data) {
                mView.setShowSendTransaction(data);
            }

            @Override
            public void OnFailed(Throwable e) {
                mView.showToast(e.getMessage());
            }
        });
    }

    public void transferContract(SmartContractMessage data){
        manager.transferContract(data,data.value, new BaseListener<String>() {
            @Override
            public void OnSuccess(String s) {
                mView.showToast(s);
            }

            @Override
            public void OnFailed(Throwable e) {
                mView.showToast(e.toString());
            }
        });
    }
}
