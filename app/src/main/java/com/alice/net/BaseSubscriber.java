package com.alice.net;

import io.reactivex.observers.DisposableObserver;

public class BaseSubscriber<T> extends DisposableObserver<T> {
    private RequestCallback<T> requestCallback;

    public BaseSubscriber(RequestCallback<T> requestCallback) {
        this.requestCallback = requestCallback;
    }
    @Override
    public void onNext(T t) {
        if (requestCallback != null) {
            requestCallback.onSuccess(t);
        }
    }

    @Override
    public void onError(Throwable e) {
        if (requestCallback != null) {
            requestCallback.OnFailed(e);
        }
    }

    @Override
    public void onComplete() {
        if(!isDisposed()){
            dispose();
        }
    }
}
