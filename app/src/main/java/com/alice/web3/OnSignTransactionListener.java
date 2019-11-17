package com.alice.web3;


import com.alice.web3.entity.Web3Transaction;

public interface OnSignTransactionListener {
    void onSignTransaction(Web3Transaction transaction, String url);
}
