package com.alice.web3;


import com.alice.web3.entity.Message;

public interface OnSignPersonalMessageListener {
    void onSignPersonalMessage(Message<String> message);
}
