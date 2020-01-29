package com.alice.bridge;

import android.util.Log;
import android.widget.Toast;

import com.alice.async.BaseListener;
import com.alice.customView.TransferDialog;
import com.alice.manager.Web3jManager;
import com.alice.utils.Hex;
import com.alice.utils.LogUtil;
import com.alice.utils.ToastUtils;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Convert;

import java.util.List;

import javax.annotation.Nonnull;

public class WalletModule extends ReactContextBaseJavaModule {

    private TransferDialog transformDialog;

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
        if(transformDialog == null){
            transformDialog = new TransferDialog(getCurrentActivity(),to,value);
            transformDialog.setOnClickConfirmListener(new TransferDialog.OnClickConfirmListener() {
                @Override
                public void onClickConfirm(String address, String value) {
                    Web3jManager.getInstance().transfer(address, value, new BaseListener<TransactionReceipt>() {
                        @Override
                        public void OnSuccess(TransactionReceipt send) {
                            String text = "Transaction complete:" + "trans hash=" + send.getTransactionHash() + "from :" + send.getFrom() + "to:" + send.getTo() + "gas used=" + send.getGasUsed() + "status: " + send.getStatus();
                            LogUtil.d(text);
                            promise.resolve(send.getTransactionHash());
                        }

                        @Override
                        public void OnFailed(Throwable e) {
                            LogUtil.e(e.toString());
                            promise.reject(e.toString());
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
        }
        transformDialog.show();
    }


    @ReactMethod
    public void signMessage(String message,Promise promise) {
        Log.d("zhhr1122","message:" + message);
        String signString = Hex.hexToUtf8(message);
        ToastUtils.makeText(signString);
        Web3jManager.getInstance().sign(message,new BaseListener<String>() {

            @Override
            public void OnSuccess(String s) {
                promise.resolve(s);
            }

            @Override
            public void OnFailed(Throwable e) {
                promise.reject(e.toString());
            }
        });
    }
}
