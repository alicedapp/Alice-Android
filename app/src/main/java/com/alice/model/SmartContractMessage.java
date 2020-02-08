package com.alice.model;

import java.math.BigInteger;

/**
 * @Description:
 * @Author: zhanghaoran3
 * @CreateDate: 2020/1/23
 */
public class SmartContractMessage {

    public GasPriceModel gasPriceModel;

    public  PriceModel priceModel;

    public BigInteger gasLimit;

    public BigInteger nonce;

    public String funcABI;

    public BigInteger gasPrice;

    public String contractAddr;

    public String fromAddr;

    public String value;

    @Override
    public String toString() {
        return "SmartContractMessage{" +
                "gasPriceModel=" + gasPriceModel +
                ", priceModel=" + priceModel +
                ", gasLimit=" + gasLimit +
                ", funcABI='" + funcABI + '\'' +
                ", gasPrice=" + gasPrice +
                ", contractAddr='" + contractAddr + '\'' +
                '}';
    }
}
