package com.alice.presenter;

import android.app.Activity;

import com.alice.async.BaseListener;
import com.alice.presenter.base.BasePresenter;
import com.alice.view.IImportView;

import org.web3j.crypto.Credentials;

public class ImportPresenter extends BasePresenter<IImportView> {

    public ImportPresenter(Activity context, IImportView view){
        super(context,view);
    }


    public void importWallet(String memorizingWords) {
        manager.importWallet(memorizingWords, new BaseListener<Credentials>() {
            @Override
            public void OnSuccess(Credentials credentials) {
                mView.importSuccess(credentials);
            }

            @Override
            public void OnFailed(Throwable e) {
                mView.importFailed(e);
            }
        });
    }
}
