package com.giligans.queueapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.giligans.queueapp.activities.MainApp;
import com.giligans.queueapp.activities.Pay;
import com.giligans.queueapp.R;
import com.giligans.queueapp.adapters.QueueAdapter;
import com.giligans.queueapp.models.QueueModel;

import java.util.ArrayList;

public class LineFragment extends Fragment {
    public RecyclerView customerRecycler;
    public TextView empty;
    public QueueAdapter queueAdapter;
    ArrayList<QueueModel> customer;
    public Button pay;
    String customer_id;
    String queue_id;

    public LineFragment(){ }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_queue, container, false);
        customerRecycler = (RecyclerView) view.findViewById(R.id.lineRecyclerView);
        empty = (TextView) view.findViewById(R.id.empty);
        customer = new ArrayList<>();
        pay = (Button) view.findViewById(R.id.payBtn);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("login", Context.MODE_PRIVATE);
        customer_id = !((MainApp)getActivity()).guest ? sharedPreferences.getString("keyid", null) : "0";

        int uuid = Integer.parseInt(customer_id) + (!((MainApp)getActivity()).guest ? 1000 : 2000);
        queue_id = String.valueOf(uuid);

        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent payIntent = new Intent(getContext(), Pay.class);
                payIntent.putExtra("qr", queue_id + " " + customer_id);
                startActivity(payIntent);
            }
        });
        pay.setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1, GridLayoutManager.VERTICAL, false);
        customerRecycler.setLayoutManager(gridLayoutManager);
        customerRecycler.setItemAnimator(null);
        queueAdapter = new QueueAdapter(getContext(), customer);
        customerRecycler.setAdapter(queueAdapter);
    }
}