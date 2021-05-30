package com.giligans.queueapp;

<<<<<<< HEAD
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.giligans.queueapp.models.CustomerModel;

=======
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import com.giligans.queueapp.models.CustomerModel;
>>>>>>> 9e3b33763f3e6ef32080d1b0fffeea1543eb516c
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
<<<<<<< HEAD
        return newList.get(newItemPosition).getId() == oldList.get(oldItemPosition).getId();
=======
        return newList.get(newItemPosition).id == oldList.get(oldItemPosition).id;
>>>>>>> 9e3b33763f3e6ef32080d1b0fffeea1543eb516c
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
<<<<<<< HEAD
        final CustomerModel oldCustomer = oldList.get(oldItemPosition);
        final CustomerModel newCustomer = newList.get(newItemPosition);

        return oldCustomer.getName().equals(newCustomer.getName());
=======
        int result = newList.get(newItemPosition).compareTo(oldList.get(oldItemPosition));
        return result == 0;
>>>>>>> 9e3b33763f3e6ef32080d1b0fffeea1543eb516c
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
<<<<<<< HEAD
        return super.getChangePayload(oldItemPosition, newItemPosition);
=======
        CustomerModel newModel = newList.get(newItemPosition);
        CustomerModel oldModel = oldList.get(oldItemPosition);

        Bundle diff = new Bundle();

        if (newModel.name!= (oldModel.name)) {
            diff.putString("name", newModel.name);
        }
        if (diff.size() == 0) {
            return null;
        }
        return diff;
        //return super.getChangePayload(oldItemPosition, newItemPosition);
>>>>>>> 9e3b33763f3e6ef32080d1b0fffeea1543eb516c
    }
}
