package com.giligans.queueapp.models;

import java.util.ArrayList;

public class CustomerModel {
    String id;
    String queueNumber;
    String name;
    ArrayList<PlateModel> orders;

    public CustomerModel(String id, String queueNumber, String name, ArrayList<PlateModel> orders) {
        this.id = id;
        this.queueNumber = queueNumber;
        this.name = name;
        this.orders = orders;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQueueNumber() {
        return queueNumber;
    }

    public void setQueueNumber(String queueNumber) {
        this.queueNumber = queueNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<PlateModel> getOrders() {
        return orders;
    }

    public void setOrders(ArrayList<PlateModel> orders) {
        this.orders = orders;
    }
}
