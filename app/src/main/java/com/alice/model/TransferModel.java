package com.alice.model;

import java.math.BigInteger;

/**
 * @Description:
 * @Author: zhanghaoran3
 * @CreateDate: 2020/1/23
 */
public class TransferModel {

    public GasPriceModel gasPriceModel;

    public  PriceModel priceModel;

    public BigInteger gasLimit;

    public BigInteger nonce;

    public String funcABI;

    public BigInteger gasPrice;

    public String contractAddr;

    public String fromAddr;

}
