package com.alice.source;

import com.alice.model.BaseReponseBody;
import com.alice.net.AliceApi;
import com.alice.net.BaseSubscriber;
import com.alice.net.Function.BaseReponseFunction;
import com.alice.net.RequestCallback;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Response;

public class BaseDataSource {
    private CompositeDisposable compositeDisposable;

    public BaseDataSource(){
        compositeDisposable = new CompositeDisposable();
    }

    public BaseDataSource(CompositeDisposable compositeDisposable){
        this.compositeDisposable = compositeDisposable;
    }

    public <T> T getService(Class<T> clz) {
        return AliceApi.getInstance().create(clz);
    }


    public <T> void execute(Observable observable,  RequestCallback<T> callback) {
        Disposable disposable = (Disposable) observable
                .map(new BaseReponseFunction<BaseReponseBody<T>>())
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new BaseSubscriber<>(callback));
        compositeDisposable.add(disposable);
    }

    public <T> void execute1(Observable observable,  RequestCallback<T> callback) {
        Disposable disposable = (Disposable) observable
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new BaseSubscriber<>(callback));
        compositeDisposable.add(disposable);
    }

    public <T> T executeSync(Call<T> call) throws IOException{
        Response<T> response = call.execute();
        return response.body();
    }

    public void clear() {
        if (!compositeDisposable.isDisposed()) {
            compositeDisposable.dispose();
        }
    }

}
