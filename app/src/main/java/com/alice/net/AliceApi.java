package com.alice.net;


import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class AliceApi {

    private static volatile AliceApi sInstance;
    private Retrofit mRetrofit;

    public static AliceApi getInstance() {
        if (sInstance == null) {
            synchronized (AliceApi.class) {
                if (sInstance == null) {
                    sInstance = new AliceApi();
                }
            }
        }
        return sInstance;
    }

    private AliceApi() {
        initRetrofit();
    }

    private void initRetrofit() {
        mRetrofit = new Retrofit.Builder()
                .baseUrl(ApiConstants.BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(createOkHttpClient())
                .build();
    }

    private OkHttpClient createOkHttpClient() {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        clientBuilder.connectTimeout(15, TimeUnit.SECONDS);
        clientBuilder.addInterceptor(chain -> {
            Request originalRequest = chain.request();
            Request.Builder builder = originalRequest.newBuilder()
                    .addHeader("Accept", "application/json")
                    .addHeader("X-CMC_PRO_API_KEY", "708fac2a-a6fa-43b2-861f-ce58a82f74b5");
            return chain.proceed(builder.build());
        });
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        clientBuilder.addInterceptor(loggingInterceptor);
        return clientBuilder.build();
    }

    public <T> T create(Class<T> clazz) {
        if (mRetrofit == null) {
            initRetrofit();
        }
        Retrofit retrofit = mRetrofit;
        return retrofit.create(clazz);
    }

}
