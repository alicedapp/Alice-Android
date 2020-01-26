package com.alice.presenter.base;

import android.app.Activity;

import com.alice.manager.Web3jManager;
import com.alice.view.IBaseView;

public abstract class BasePresenter<GV extends IBaseView> {

    protected GV mView;
    protected Activity mContext;
    protected Web3jManager manager;

    public BasePresenter(Activity context, GV view) {
        mContext = context;
        mView = view;
        manager = Web3jManager.getInstance();
    }

    protected BasePresenter() {

    }

    public void onDestroy(){

    }

}
