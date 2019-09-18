package com.alice.activity.base;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.alice.presenter.base.BasePresenter;

import butterknife.ButterKnife;


public abstract class BaseActivity<P extends BasePresenter> extends Activity {
    protected P mPresenter;

    protected abstract void initPresenter(Intent intent);
    //设置布局
    protected abstract int getLayout();
    //初始化布局
    protected abstract void initView();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
        setContentView(getLayout());
        initPresenter(getIntent());
        checkPresenterIsNull();
        ButterKnife.bind(this);
        initView();
    }

    private void checkPresenterIsNull() {
        if (mPresenter == null) {
            throw new IllegalStateException("please init mPresenter in initPresenter() method ");
        }
    }

    //设置打印方法
    public void toast(String text){
        Toast.makeText(this,text,Toast.LENGTH_LONG).show();
    }

}
