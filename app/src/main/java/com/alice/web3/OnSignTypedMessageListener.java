package com.alice.web3;

import com.alice.web3.entity.Message;
import com.alice.web3.entity.TypedData;

public interface OnSignTypedMessageListener {
    void onSignTypedMessage(Message<TypedData[]> message);
}
