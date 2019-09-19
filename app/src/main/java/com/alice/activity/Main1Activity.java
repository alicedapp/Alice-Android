package com.alice.activity;

import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.alice.R;
import com.alice.activity.base.BaseActivity;
import com.alice.customView.BaseDialog;
import com.alice.customView.TransferDialog;
import com.alice.presenter.MainPresenter;
import com.alice.view.IMainView;

import butterknife.OnClick;

/**
 * create by zhhr on 2019/09/18
 */
public class Main1Activity extends BaseActivity<MainPresenter> implements IMainView {
    private TransferDialog transformDialog;

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

    }

    @Override
    @OnClick({R.id.toRn})
    public void toRn() {
        startActivity(new Intent(Main1Activity.this,RnActivity.class));
    }

    @Override
    @OnClick({R.id.createWallet})
    public void createWallet() {
        mPresenter.createWallet();
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
    @OnClick({R.id.transfer})
    public void transfer() {
        if(transformDialog == null){
            transformDialog = new TransferDialog(Main1Activity.this);
            transformDialog.setOnClickConfirmListener(new TransferDialog.OnClickConfirmListener() {
                @Override
                public void onClickConfirm(String address, String value) {
                    mPresenter.transfer(address,value);
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

    @Override
    public void showToast(String t) {
        toast(t);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mPresenter.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }
}
