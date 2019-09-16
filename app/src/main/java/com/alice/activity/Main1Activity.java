package com.alice.activity;

import android.content.Intent;

import com.alice.R;
import com.alice.activity.base.BaseActivity;
import com.alice.presenter.MainPresenter;
import com.alice.view.IMainView;


public class Main1Activity extends BaseActivity<MainPresenter> implements IMainView {
    @Override
    protected void initPresenter(Intent intent) {
        mPresenter = new MainPresenter();
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {

    }

    @Override
    public void toRn() {
        startActivity(new Intent(Main1Activity.this,RnActivity.class));
    }

    @Override
    public void createWallet() {

    }

    @Override
    public void importWallet() {

    }

    @Override
    public void checkBalances() {

    }

    @Override
    public void transfer(String address) {

    }

    @Override
    public void ShowToast(String t) {

    }
}
