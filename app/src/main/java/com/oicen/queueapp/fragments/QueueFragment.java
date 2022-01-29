package com.oicen.queueapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.oicen.queueapp.R;
import com.oicen.queueapp.activities.MainApp;
import com.oicen.queueapp.activities.Pay;
import com.oicen.queueapp.adapters.QueueAdapter;
import com.oicen.queueapp.models.QueueModel;

import java.util.ArrayList;
import java.util.Objects;

public class
QueueFragment extends Fragment {
    public RecyclerView customerRecycler;
    public TextView empty;
    public QueueAdapter queueAdapter;
    ArrayList<QueueModel> customer;
    public Button pay;
    String customer_id;
    String queue_id;
    public ShimmerFrameLayout shimmerFrameLayout;

    public QueueFragment(){ }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_queue, container, false);
        customerRecycler = (RecyclerView) view.findViewById(R.id.lineRecyclerView);
        empty = (TextView) view.findViewById(R.id.empty);
        customer = new ArrayList<>();
        pay = (Button) view.findViewById(R.id.payBtn);
        shimmerFrameLayout = (ShimmerFrameLayout) view.findViewById(R.id.shimmerLayout);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("login", Context.MODE_PRIVATE);
        customer_id = sharedPreferences.getString("keyid", null);

        int uuid = Integer.parseInt(customer_id) + 1000;
        queue_id = String.valueOf(uuid);

        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent payIntent = new Intent(getContext(), Pay.class);
                payIntent.putExtra("qr", queue_id + " " + customer_id);
                startActivityForResult(payIntent, 1);
            }
        });
        pay.setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1){
            Bundle values = data.getExtras();
            if(values != null){
                String message = data.getStringExtra("result");
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            }
        }
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