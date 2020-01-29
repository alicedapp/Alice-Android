package com.alice.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.widget.TextView;

import com.alice.R;
import com.alice.activity.base.BaseActivity;
import com.alice.customView.BaseDialog;
import com.alice.presenter.LoginPresenter;
import com.alice.view.ILoginView;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @Description: 登录入口
 * @Author: zhanghaoran3
 * @CreateDate: 2020/1/26
 */
public class LoginActivity extends BaseActivity<LoginPresenter> implements ILoginView {
    @BindView(R.id.tv_create)
    TextView mCreate;
    @BindView(R.id.tv_import)
    TextView mImport;

    @Override
    protected void initPresenter(Intent intent) {
        mPresenter = new LoginPresenter(this,this);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_login;
    }

    @Override
    protected void initView() {

    }

    @OnClick(R.id.tv_create)
    public void createWallet() {
        mPresenter.createWallet();
        if(progressDialog!=null){
            progressDialog.setMessage("alice is creating wallet");
            progressDialog.show();
        }
    }

    @OnClick(R.id.tv_import)
    public void importWallet() {
        startActivity(new Intent(this,ImportActivity.class));
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
    public void createSuccess() {
        if(progressDialog!=null){
            progressDialog.dismiss();
        }
        finish();
        startActivity(new Intent(this,Main1Activity.class));
    }

    @Override
    public void createFailed(String message) {
        toast(message);
    }
}
