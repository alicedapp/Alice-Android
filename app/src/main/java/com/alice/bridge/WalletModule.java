package com.alice.bridge;

import android.app.ProgressDialog;
import android.widget.Toast;

import com.alice.async.BaseListener;
import com.alice.async.MainHandler;
import com.alice.customView.BaseBottomView;
import com.alice.customView.SendTransactionView;
import com.alice.customView.SignMessageView;
import com.alice.customView.TransferDialog;
import com.alice.manager.Web3jManager;
import com.alice.model.SmartContractMessage;
import com.alice.utils.ToastUtils;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.utils.Convert;

import javax.annotation.Nonnull;

public class WalletModule extends ReactContextBaseJavaModule {

    private TransferDialog transformDialog;
    private SignMessageView signMessageView;
    private SendTransactionView sendTransactionView;

    public WalletModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Nonnull
    @Override
    public String getName() {
        return "WalletModule";
    }

    @ReactMethod
    public void getAddress(Promise promise) {
        Web3jManager.getInstance().importWallet(new BaseListener<Credentials>() {
            @Override
            public void OnSuccess(Credentials credentials) {
                promise.resolve(credentials.getAddress());
            }

            @Override
            public void OnFailed(Throwable e) {
                promise.reject(e.toString());
            }
        });
    }

    @ReactMethod
    public void getBalance(Promise promise) {
        Web3jManager.getInstance().checkBalances(new BaseListener<EthGetBalance>() {

            @Override
            public void OnSuccess(EthGetBalance balance) {
                String blanceETH = Convert.fromWei(balance.getBalance().toString(), Convert.Unit.ETHER).toPlainString().concat("ether");
                Toast.makeText(getReactApplicationContext(),"getBalance:" + blanceETH,Toast.LENGTH_LONG).show();
                promise.resolve(blanceETH);
            }

            @Override
            public void OnFailed(Throwable e) {
                promise.reject(e.toString());
            }
        });
    }

    @ReactMethod
    public void transfer(String to,String value,Promise promise) {
        transformDialog = new TransferDialog(getCurrentActivity(),to,value);
        transformDialog.setOnClickConfirmListener(new TransferDialog.OnClickConfirmListener() {
            @Override
            public void onClickConfirm(String address, String value) {
                sendTransactionView = new SendTransactionView(getCurrentActivity());
                sendTransactionView.showView(getCurrentActivity());
                Web3jManager.getInstance().loadTransferInfo(address, value, new BaseListener<SmartContractMessage>() {
                    @Override
                    public void OnSuccess(SmartContractMessage data) {
                        sendTransactionView.setData(data);
                        sendTransactionView.setOnClickSendListener(new BaseBottomView.OnClickSendListener<SmartContractMessage>() {
                            @Override
                            public void OnClickSend(SmartContractMessage data) {
                                sendTransactionView.hideView();
                                Web3jManager.getInstance().transferContract(data, data.value, new BaseListener<String>() {
                                    @Override
                                    public void OnSuccess(String s) {
                                        ToastUtils.makeText("transfer success" + s);
                                        promise.resolve(s);
                                    }

                                    @Override
                                    public void OnFailed(Throwable e) {
                                        promise.reject(e.getMessage());
                                    }
                                });
                            }
                        });
                    }

                    @Override
                    public void OnFailed(Throwable e) {
                    }
                });
            }

            @Override
            public void onAddressError(String message) {
                promise.reject(message);
            }

            @Override
            public void onValueError(String message) {
                promise.reject(message);
            }
        });
        transformDialog.show();
    }


    @ReactMethod
    public void signMessage(String message,Promise promise) {
        MainHandler.getInstance().post(() -> {
            if(signMessageView ==  null){
                signMessageView = new SignMessageView(getCurrentActivity());
                signMessageView.setOnClickSendListener(data -> Web3jManager.getInstance().sign(message,new BaseListener<String>() {

                    @Override
                    public void OnSuccess(String s) {
                        promise.resolve(s);
                        signMessageView.hideView();
                    }

                    @Override
                    public void OnFailed(Throwable e) {
                        promise.reject(e.toString());
                        signMessageView.hideView();
                    }
                }));
            }
            signMessageView.setData(message);
            signMessageView.showView(getCurrentActivity());
        });
    }

    @ReactMethod
    public void getNetwork(Promise promise) {
        promise.resolve("{\"rpcURL\": \"http://\", \"name\": \"main\", \"color\": \"#123121\"}");
    }

}
