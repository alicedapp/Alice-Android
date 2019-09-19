package com.alice.view;

import org.web3j.crypto.Credentials;

public interface IMainView extends IBaseView{
    void toRn();
    void createWallet();
    void importWallet();
    void checkBalances();
    void transfer();
    void showContent(Credentials credentials);
    void showCreateDialog();
}
