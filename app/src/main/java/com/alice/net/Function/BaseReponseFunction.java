package com.alice.net.Function;

import com.alice.model.BaseReponseBody;

import io.reactivex.functions.Function;


public class BaseReponseFunction<T> implements Function<BaseReponseBody<T>,T> {
    @Override
    public T apply(BaseReponseBody<T> reponseBody) throws Exception {
        if(reponseBody.status.getError_code()!=0){
            throw new Exception(reponseBody.status.getError_message());
        }
        return reponseBody.data.get(0);
    }
}
