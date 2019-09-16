package com.alice.view;

public interface IMainView extends IBaseView{
    void toRn();
    void createWallet();
    void importWallet();
    void checkBalances();
    void transfer(String address);
}
