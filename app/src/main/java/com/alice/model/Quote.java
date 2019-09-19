package com.alice.model;

public class Quote {

    private CountryPriceModel CNY;
    private CountryPriceModel USD;

    public void setCNY(CountryPriceModel CNY) {
        this.CNY = CNY;
    }
    public CountryPriceModel getCNY() {
        return CNY;
    }

    public CountryPriceModel getUSD() {
        return USD;
    }

    public void setUSD(CountryPriceModel USD) {
        this.USD = USD;
    }
}