package com.alice.bridge;

import android.util.Log;
import android.widget.Toast;

import com.alice.async.BaseListener;
import com.alice.async.MainHandler;
import com.alice.customView.BottomTapView;
import com.alice.manager.Web3jManager;
import com.alice.model.SmartContractMessage;
import com.alice.utils.ToastUtils;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;

import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class ContractModule extends ReactContextBaseJavaModule {
    private BottomTapView mBottomTapView;

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
        String finalContractAddress = "0x2f21957c7147c3eE49235903D6471159a16c9ccd";
        MainHandler.getInstance().post(() -> {
            mBottomTapView = new BottomTapView(getCurrentActivity());
            List<Object> params = parameters.toArrayList();
            String[] arrayParams = new String[params.size()];
            for(int i=0;i<parameters.size();i++){
                arrayParams[i] = (String) params.get(i);
            }
            mBottomTapView.showView(getCurrentActivity(), finalContractAddress,functionName,value,arrayParams);
            List<Type> inputArgs = new ArrayList<>();
            for(Object param:params){
                inputArgs.add(new Utf8String((String) param));
            }
            List<TypeReference<?>> outputArgs = new ArrayList<>();
            Web3jManager.getInstance().loadSmartContractSet(finalContractAddress, functionName, value, inputArgs, outputArgs, new BaseListener<SmartContractMessage>() {
                @Override
                public void OnSuccess(SmartContractMessage smartContractMessage) {
                    mBottomTapView.setData(smartContractMessage);
                }

                @Override
                public void OnFailed(Throwable e) {
                    mBottomTapView.hideView();
                    promise.reject(e.getMessage());
                }
            });
            mBottomTapView.setOnClickSendListener(data1 -> Web3jManager.getInstance().setSmartContract(data1, new BaseListener<String>() {
                @Override
                public void OnSuccess(String s) {
                    promise.resolve(s);
                    mBottomTapView.hideView();
                }

                @Override
                public void OnFailed(Throwable e) {
                    promise.reject(e.getMessage());
                    mBottomTapView.hideView();
                }
            }));
        });
    }

    @ReactMethod
    public void read(String contractAddress, String abi, String functionName, ReadableArray parameters, Promise promise) {
        ToastUtils.makeText("contractAddress" + contractAddress);
     /*   MainHandler.getInstance().post(new Runnable() {
            @Override
            public void run() {
                ToastUtils.makeText("test啊啊啊");
                List<Type> inputParameters = new ArrayList<>();
                List<Object> params = parameters.toArrayList();
                for(Object param:params){
                    inputParameters.add(new Utf8String((String) param));
                }
                List<TypeReference<?>> outputParameters = new ArrayList<>();
                TypeReference<Utf8String> typeReference = new TypeReference<Utf8String>() {};
                outputParameters.add(typeReference);
                Web3jManager.getInstance().readFromContract(contractAddress, functionName, inputParameters, outputParameters, new BaseListener<String>() {
                    @Override
                    public void OnSuccess(String s) {
                        promise.resolve(s);
                    }

                    @Override
                    public void OnFailed(Throwable e) {
                        promise.reject(e.getMessage());
                    }
                });

            }
        });*/
    }
}
