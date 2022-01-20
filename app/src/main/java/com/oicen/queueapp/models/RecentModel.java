package com.oicen.queueapp.models;

import static java.lang.Integer.parseInt;

import com.oicen.queueapp.fragments.RecentOrdersFragment;

import java.util.ArrayList;

public class RecentModel {
    public ArrayList<RecentOrderItem> object;
    public String id;
    public String date;

    public RecentModel(ArrayList<RecentOrderItem> object) {
        this.object = object;
    }

    public ArrayList<RecentOrderItem> getObject() {
        return object;
    }

    public void setObject(ArrayList<RecentOrderItem> object) {
        this.object = object;
    }

    public String getId() {
        return object.get(0).getId();
    }

    public void setId(String id) {
        this.object.get(0).setId(id);
    }

    public String getDate() {
        return object.get(0).getDate();
    }

    public void setDate(String date) {
        this.object.get(0).setDate(date);
    }

    public String getTotal() {
        int total = 0;
        for (RecentOrderItem obj: object) {
            total = total + parseInt(obj.getTotal());
        }

        return String.valueOf(total);
    }

    public void setTotal(int pos, String total) {
        this.object.get(pos).setTotal(total);
    }
}