package com.alice.model;

import java.util.Date;
import java.util.List;

public class PriceModel {

    private int id;
    private String name;
    private String symbol;
    private String slug;
    private int num_market_pairs;
    private Date date_added;
    private List<String> tags;
    private String max_supply;
    private double circulating_supply;
    private double total_supply;
    private String platform;
    private int cmc_rank;
    private Date last_updated;
    private Quote quote;
    public void setId(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    public String getSymbol() {
        return symbol;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }
    public String getSlug() {
        return slug;
    }

    public void setNum_market_pairs(int num_market_pairs) {
        this.num_market_pairs = num_market_pairs;
    }
    public int getNum_market_pairs() {
        return num_market_pairs;
    }

    public void setDate_added(Date date_added) {
        this.date_added = date_added;
    }
    public Date getDate_added() {
        return date_added;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    public List<String> getTags() {
        return tags;
    }

    public void setMax_supply(String max_supply) {
        this.max_supply = max_supply;
    }
    public String getMax_supply() {
        return max_supply;
    }

    public void setCirculating_supply(double circulating_supply) {
        this.circulating_supply = circulating_supply;
    }
    public double getCirculating_supply() {
        return circulating_supply;
    }

    public void setTotal_supply(double total_supply) {
        this.total_supply = total_supply;
    }
    public double getTotal_supply() {
        return total_supply;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }
    public String getPlatform() {
        return platform;
    }

    public void setCmc_rank(int cmc_rank) {
        this.cmc_rank = cmc_rank;
    }
    public int getCmc_rank() {
        return cmc_rank;
    }

    public void setLast_updated(Date last_updated) {
        this.last_updated = last_updated;
    }
    public Date getLast_updated() {
        return last_updated;
    }

    public void setQuote(Quote quote) {
        this.quote = quote;
    }
    public Quote getQuote() {
        return quote;
    }

}