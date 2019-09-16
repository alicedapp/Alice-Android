package com.alice.presenter.base;

import android.app.Activity;

import com.alice.view.IBaseView;

public abstract class BasePresenter<GV extends IBaseView> {

    protected GV mView;
    protected Activity mContext;

    public BasePresenter(Activity context, GV view) {
        mContext = context;
        mView = view;
    }

    protected BasePresenter() {

    }

}
