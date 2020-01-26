package com.alice.view;

import org.web3j.crypto.Credentials;

public interface IImportView extends IBaseView{

    void importSuccess(Credentials credentials);

    void importFailed(Throwable throwable);
}
