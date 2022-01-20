package com.oicen.queueapp.models;

public class RecentOrderItem {
    public String id;
    public String date;
    public String item_name;
    public String qty;
    public String price;
    public String total;

    public RecentOrderItem(String id, String date, String item_name, String qty, String price, String total){
        this.id = id;
        this.date = date;
        this.item_name = item_name;
        this.qty = qty;
        this.price = price;
        this.total = total;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getItem_name() {
        return item_name;
    }

    public void setItem_name(String item_name) {
        this.item_name = item_name;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }
}
