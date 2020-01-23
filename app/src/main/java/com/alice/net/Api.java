package com.alice.net;

import com.alice.model.BaseReponseBody;
import com.alice.model.GasPriceModel;
import com.alice.model.PriceModel;

import io.reactivex.Observable;
import io.reactivex.Single;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface Api {
    @GET
    Observable<BaseReponseBody<PriceModel>> getPriceModel(@Url String url,@Query("start")int start,@Query("limit")int limit,@Query("convert")String convert);

    @GET
    Observable<GasPriceModel> getGasPriceModel(@Url String url);

    @GET
    Call<GasPriceModel> getGasPriceModelSync(@Url String url);

    @GET
    Call<BaseReponseBody<PriceModel>> getPriceModelSync(@Url String url,@Query("start")int start,@Query("limit")int limit,@Query("convert")String convert);
}
