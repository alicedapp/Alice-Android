package com.alice.activity;

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

    private BaseDialog createDialog;

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

    @Override
    @OnClick(R.id.tv_create)
    public void createWallet() {
        mPresenter.checkWallet();
    }

    @Override
    @OnClick(R.id.tv_import)
    public void importWallet() {
        startActivity(new Intent(this,ImportActivity.class));
    }

    @Override
    public void showCreateDialog() {
        if(createDialog == null){
            createDialog = new BaseDialog.Builder(this)
                    .setTitle("Replace the key")
                    .setMessage("You created the key already,Do you want to replace it？")
                    .setPositiveButton("Replace", v -> {
                        mPresenter.createWallet();
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
    public void showToast(String t) {
        toast(t);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mPresenter.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }
}
