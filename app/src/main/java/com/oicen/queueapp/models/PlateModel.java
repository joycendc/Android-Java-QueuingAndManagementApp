package com.oicen.queueapp.models;

import java.io.Serializable;

public class PlateModel implements Serializable {
    String name, price, bigimageurl;
    int qty, total;

    public PlateModel(String name, String price, int qty, int total, String bigimageurl) {
        this.name = name;
        this.price = price;
        this.qty = qty;
        this.total = total;
        this.bigimageurl = bigimageurl;
    }

    public String getBigimageurl() {
        return bigimageurl;
    }

    public void setBigimageurl(String bigimageurl) {
        this.bigimageurl = bigimageurl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}