package com.oicen.queueapp.models;

public class OrderModel {
    String name, qty, total, price;

    public OrderModel(String name, String qty, String total, String price) {
        this.name = name;
        this.qty = qty;
        this.total = total;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
