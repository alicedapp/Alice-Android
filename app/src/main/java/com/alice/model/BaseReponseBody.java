package com.alice.model;

import java.util.List;

public class BaseReponseBody<T>{
    public List<T> data;
    public Status status;
}
