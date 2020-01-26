package com.alice.bridge;

import android.util.Log;
import android.widget.Toast;

import com.alice.async.BaseListener;
import com.alice.manager.Web3jManager;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;

import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthSendTransaction;

import javax.annotation.Nonnull;

public class ContractModule extends ReactContextBaseJavaModule {

    public ContractModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Nonnull
    @Override
    public String getName() {
        return "ContractModule";
    }

    @ReactMethod
    public void write(String contractAddress, String abi, String functionName, ReadableArray parameters, String value, String data, Promise promise) {
        Toast.makeText(getReactApplicationContext(),"contractAddress:"+ contractAddress,Toast.LENGTH_LONG).show();
       /* Web3jManager.getInstance().callSmartContractFunction(contractAddress, abi, functionName, parameters, value, data, new BaseListener<EthCall>() {


            @Override
            public void OnSuccess(EthCall ethCall) {
                Toast.makeText(getReactApplicationContext(),ethCall.getValue(),Toast.LENGTH_LONG).show();
            }

            @Override
            public void OnFailed(Throwable e) {
                Toast.makeText(getReactApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });*/
        Log.d("zhhr1122","contractAddress:" + contractAddress + ",functionName:" + functionName+",parameters = " + parameters  + ",value:" + value + ",data:" + data);
    }
}
