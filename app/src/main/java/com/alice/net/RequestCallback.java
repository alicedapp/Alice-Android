package com.alice.net;

public interface RequestCallback<T>{
    void onSuccess(T t);
    void OnFailed(Throwable throwable);
}
