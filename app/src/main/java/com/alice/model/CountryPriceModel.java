package com.alice.model;
import java.util.Date;

public class CountryPriceModel {

    private double price;
    private double volume_24h;
    private String percent_change_1h;
    private String percent_change_24h;
    private String percent_change_7d;
    private double market_cap;
    private Date last_updated;

    public void setPrice(double price) {
        this.price = price;
    }
    public double getPrice() {
        return price;
    }

    public void setVolume_24h(double volume_24h) {
        this.volume_24h = volume_24h;
    }
    public double getVolume_24h() {
        return volume_24h;
    }

    public void setPercent_change_1h(String percent_change_1h) {
        this.percent_change_1h = percent_change_1h;
    }
    public String getPercent_change_1h() {
        return percent_change_1h;
    }

    public void setPercent_change_24h(String percent_change_24h) {
        this.percent_change_24h = percent_change_24h;
    }
    public String getPercent_change_24h() {
        return percent_change_24h;
    }

    public void setPercent_change_7d(String percent_change_7d) {
        this.percent_change_7d = percent_change_7d;
    }
    public String getPercent_change_7d() {
        return percent_change_7d;
    }

    public void setMarket_cap(double market_cap) {
        this.market_cap = market_cap;
    }
    public double getMarket_cap() {
        return market_cap;
    }

    public void setLast_updated(Date last_updated) {
        this.last_updated = last_updated;
    }
    public Date getLast_updated() {
        return last_updated;
    }

}