package com.alice.activity;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.alice.R;
import com.alice.activity.base.BaseActivity;
import com.alice.presenter.ImportPresenter;
import com.alice.view.IImportView;

import org.web3j.crypto.Credentials;

import butterknife.BindView;

/**
 * @Description: 导入入口
 * @Author: zhanghaoran3
 * @CreateDate: 2020/1/26
 */
public class ImportActivity extends BaseActivity<ImportPresenter> implements IImportView {
    @BindView(R.id.et_mnemonic)
    EditText mMnemonic;

    @Override
    protected void initPresenter(Intent intent) {
        mPresenter = new ImportPresenter(this,this);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_import;
    }

    public void importWallet(View view){
        if(TextUtils.isEmpty(mMnemonic.getText())){
           showToast("Please enter memorizingWords");
           return;
        }
        mPresenter.importWallet(mMnemonic.getText().toString());

    }

    @Override
    protected void initView() {

    }

    @Override
    public void showToast(String t) {
        toast(t);
    }

    @Override
    public void importSuccess(Credentials credentials) {
        showToast("import success ,address is"+credentials.getAddress());
        startActivity(new Intent(this,Main1Activity.class));
    }

    @Override
    public void importFailed(Throwable throwable) {
        showToast(throwable.getMessage());
    }
}
