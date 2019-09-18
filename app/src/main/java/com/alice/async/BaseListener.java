package com.alice.async;

public abstract interface BaseListener<T> {
    void OnSuccess(T t);
    void OnFailed(Throwable e);
}
