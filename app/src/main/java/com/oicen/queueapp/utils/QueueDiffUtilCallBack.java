package com.oicen.queueapp.utils;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.oicen.queueapp.models.QueueModel;

import java.util.ArrayList;

public class QueueDiffUtilCallBack extends DiffUtil.Callback {
    ArrayList<QueueModel> newList;
    ArrayList<QueueModel> oldList;

    public QueueDiffUtilCallBack(ArrayList<QueueModel> newList, ArrayList<QueueModel> oldList) {
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
        final QueueModel oldCustomer = oldList.get(oldItemPosition);
        final QueueModel newCustomer = newList.get(newItemPosition);

        return oldCustomer.getName().equals(newCustomer.getName());
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
