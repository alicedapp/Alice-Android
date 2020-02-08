package com.alice.activity;

import android.content.Intent;
import android.widget.TextView;

import com.alice.R;
import com.alice.activity.base.BaseActivity;
import com.alice.config.Constants;
import com.alice.customView.BaseBottomView;
import com.alice.customView.BaseDialog;
import com.alice.customView.BottomTapView;
import com.alice.customView.SendTransactionView;
import com.alice.customView.SignMessageView;
import com.alice.customView.TransferDialog;
import com.alice.manager.Web3jManager;
import com.alice.model.SmartContractMessage;
import com.alice.presenter.MainPresenter;
import com.alice.view.IMainView;
import com.orhanobut.hawk.Hawk;

import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.crypto.Credentials;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * create by zhhr on 2019/09/18
 */
public class Main1Activity extends BaseActivity<MainPresenter> implements IMainView {
    @BindView(R.id.address)
    TextView mTvAddress;

    private TransferDialog transformDialog;
    private BaseDialog createDialog;

    private BottomTapView mBottomTapView;

    private SignMessageView mSignMessage;

    private SendTransactionView mSendTransactionView;

    @Override
    protected void initPresenter(Intent intent) {
        mPresenter = new MainPresenter(this,this);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        if(Web3jManager.getInstance().isImport()){
            mTvAddress.setText("address：" + Hawk.get(Constants.KEY_ADDRESS));
        }else{
            mPresenter.importWallet();
        }
    }

    @Override
    @OnClick({R.id.toRn})
    public void toRn() {
        startActivity(new Intent(Main1Activity.this,RnActivity.class));
    }

    @Override
    @OnClick({R.id.createWallet})
    public void createWallet() {
        mPresenter.checkWallet();
    }

    @Override
    @OnClick({R.id.importWallet})
    public void importWallet() {
        mPresenter.importWallet();
    }

    @Override
    @OnClick({R.id.checkBalances})
    public void checkBalances() {
        mPresenter.checkBalances();
    }

    @Override
    @OnClick({R.id.smartContract})
    public void smartContract() {
        mPresenter.smartContract();
    }

    @Override
    @OnClick({R.id.smartContractSet})
    public void smartContractSet() {
        mBottomTapView = new BottomTapView(this);
        mBottomTapView.showView(this,"0x2f21957c7147c3eE49235903D6471159a16c9ccd","setMessage","0",new String[]{"set new message"});
        List<Type> inputArgs = new ArrayList<>();
        inputArgs.add(new Utf8String("test : " + System.currentTimeMillis()));
        List<TypeReference<?>> outputArgs = new ArrayList<>();
        mPresenter.loadSmartContractSet("0x2f21957c7147c3eE49235903D6471159a16c9ccd","setMessage","0",inputArgs,outputArgs);
        mBottomTapView.setOnClickSendListener(new BottomTapView.OnClickSendListener() {
            @Override
            public void OnClickSend(SmartContractMessage data) {
                mPresenter.smartContractSet(data);
            }
        });
    }


    @Override
    @OnClick({R.id.transfer})
    public void transfer() {
        if(transformDialog == null){
            transformDialog = new TransferDialog(Main1Activity.this);
            transformDialog.setOnClickConfirmListener(new TransferDialog.OnClickConfirmListener() {
                @Override
                public void onClickConfirm(String address, String value) {
                    mPresenter.loadGasPrice(address,value);
                    progressDialog.setMessage("Loading");
                    progressDialog.show();
                }

                @Override
                public void onAddressError(String message) {
                    toast(message);
                }

                @Override
                public void onValueError(String message) {
                    toast(message);
                }
            });
        }
        transformDialog.show();
    }
    @OnClick({R.id.signMessage})
    public void signMessage(){
        String signMessage = "0x48656c6c6f20576f726c64000000000000000000000000000000000000000000";
        if(mSignMessage == null){
            mSignMessage = new SignMessageView(this);
            mSignMessage.setOnClickSendListener(new BaseBottomView.OnClickSendListener<String>() {
                @Override
                public void OnClickSend(String data) {
                    mPresenter.signMessage(data);
                }
            });
        }
        mSignMessage.setData(signMessage);
        mSignMessage.showView(this);
    }

    @Override
    public void showContent(Credentials credentials) {
        mTvAddress.setText("address：" + credentials.getAddress());
    }

    @Override
    public void showCreateDialog() {
        if(createDialog == null){
            createDialog = new BaseDialog.Builder(this)
                    .setTitle("Replace the key")
                    .setMessage("You created the key already,Do you want to replace it？")
                    .setPositiveButton("Replace", v -> {
                        mPresenter.createWallet();
                        mTvAddress.setText("");
                        if(createDialog!=null){
                            createDialog.dismiss();
                        }
                    })
                    .setNegativeButton("Cancel", v -> {
                        if(createDialog!=null){
                            createDialog.dismiss();
                        }
                    })
                    .create();
        }
        createDialog.show();
    }

    @Override
    public void setBottomView(SmartContractMessage smartContractMessage) {
        if(mBottomTapView!=null){
            mBottomTapView.setData(smartContractMessage);
        }
    }

    @Override
    public void setShowSendTransaction(SmartContractMessage smartContractMessage) {
        mSendTransactionView = new SendTransactionView(Main1Activity.this);
        mSendTransactionView.setData(smartContractMessage);
        mSendTransactionView.setOnClickSendListener(new BaseBottomView.OnClickSendListener<SmartContractMessage>() {
            @Override
            public void OnClickSend(SmartContractMessage data) {
                mSendTransactionView.hideView();
                mPresenter.transferContract(data);
            }
        });
        mSendTransactionView.showView(Main1Activity.this);
        progressDialog.dismiss();
    }

    @Override
    public void onSignSuccess() {
        if(mSignMessage!=null){
            mSignMessage.hideView();
        }
    }

    @Override
    public void showToast(String t) {
        toast(t);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mPresenter.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }

    @Override
    public void onBackPressed() {
        if(mBottomTapView!=null &&mBottomTapView.isShow()){
            mBottomTapView.hideView();
            return;
        }
        if(mSendTransactionView!=null &&mSendTransactionView.isShow()){
            mSendTransactionView.hideView();
            return;
        }
        if(mSignMessage!=null&&mSignMessage.isShow()){
            mSignMessage.hideView();
            return;
        }
        super.onBackPressed();

    }
}
