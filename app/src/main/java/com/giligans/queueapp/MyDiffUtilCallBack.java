package com.giligans.queueapp;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.giligans.queueapp.models.CustomerModel;

import java.util.ArrayList;

public class MyDiffUtilCallBack extends DiffUtil.Callback {
    ArrayList<CustomerModel> newList;
    ArrayList<CustomerModel> oldList;

    public MyDiffUtilCallBack(ArrayList<CustomerModel> newList, ArrayList<CustomerModel> oldList) {
        this.newList = newList;
        this.oldList = oldList;
    }

    @Override
    public int getOldListSize() {
        return oldList != null ? oldList.size() : 0;
    }

    @Override
    public int getNewListSize() {
        return newList != null ? newList.size() : 0;
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return newList.get(newItemPosition).getId() == oldList.get(oldItemPosition).getId();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        final CustomerModel oldCustomer = oldList.get(oldItemPosition);
        final CustomerModel newCustomer = newList.get(newItemPosition);

        return oldCustomer.getName().equals(newCustomer.getName());
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
