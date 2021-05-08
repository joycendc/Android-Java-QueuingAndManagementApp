package com.giligans.queueapp.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.giligans.queueapp.R;
import com.giligans.queueapp.adapters.CustomerAdapter;
import com.giligans.queueapp.models.CustomerModel;

import java.util.ArrayList;

public class LineFragment extends Fragment {

    RecyclerView customerRecycler;
    public CustomerAdapter customerAdapter;

    public LineFragment(){

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_line, container, false);
        customerRecycler = (RecyclerView) view.findViewById(R.id.lineRecyclerView);

        return view;
    }


    public void setCustomerRecycler(ArrayList<CustomerModel> customer) {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1, GridLayoutManager.VERTICAL, false);
        customerRecycler.setLayoutManager(gridLayoutManager);
        gridLayoutManager.smoothScrollToPosition(customerRecycler, null, 0);
        customerAdapter = new CustomerAdapter(getContext(), customer);
        customerRecycler.setAdapter(customerAdapter);
    }
}