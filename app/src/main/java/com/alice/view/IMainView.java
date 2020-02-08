package com.alice.view;

import com.alice.model.SmartContractMessage;

import org.web3j.crypto.Credentials;

public interface IMainView extends IBaseView{
    void toRn();
    void createWallet();
    void importWallet();
    void checkBalances();
    void transfer();
    void smartContract();
    void smartContractSet();
    void showContent(Credentials credentials);
    void showCreateDialog();
    void setBottomView(SmartContractMessage smartContractMessage);
    void setShowSendTransaction(SmartContractMessage smartContractMessage);
    void onSignSuccess();
}
