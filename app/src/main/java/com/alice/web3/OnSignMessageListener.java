package com.alice.web3;


import com.alice.web3.entity.Message;

public interface OnSignMessageListener {
    void onSignMessage(Message<String> message);
}
