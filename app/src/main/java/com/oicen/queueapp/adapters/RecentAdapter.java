package com.oicen.queueapp.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.button.MaterialButton;
import com.oicen.queueapp.R;
import com.oicen.queueapp.activities.MainApp;
import com.oicen.queueapp.fragments.ProducDetailsFragment;
import com.oicen.queueapp.models.FoodModel;
import com.oicen.queueapp.models.OrderModel;
import com.oicen.queueapp.models.RecentModel;
import com.oicen.queueapp.models.RecentOrderItem;

import java.util.ArrayList;
import java.util.List;

public class RecentAdapter extends RecyclerView.Adapter<RecentAdapter.RecentOrdersViewHolder> {
    Context context;
    ArrayList<RecentModel> recentList;
    FragmentManager fragmentManager;
    RecyclerView ordersRecycler;
    OrderAdapter orderAdapter;
    ArrayList<OrderModel> orderList;

    public RecentAdapter(Context context, FragmentManager fragmentManager, ArrayList<RecentModel> recentList) {
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.recentList = recentList;

    }

    public void updateList(ArrayList<RecentModel> recentList){
        this.recentList = null;
        this.recentList = recentList;
        notifyDataSetChanged();
    }

    @Override
    public RecentOrdersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecentOrdersViewHolder(LayoutInflater.from(context).inflate(R.layout.recent_item, parent, false));
    }


    @Override
    public void onBindViewHolder(@NonNull RecentOrdersViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        holder.date.setText(recentList.get(position).getDate());
        holder.total.setText("₱ " + recentList.get(position).getTotal());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                orderList = new ArrayList<OrderModel>();
                ArrayList<RecentOrderItem> list = recentList.get(position).getObject();

                for (RecentOrderItem item: list) {
                    orderList.add(new OrderModel(item.getItem_name(), item.getQty(), item.getTotal(), item.getPrice()));
                }

                final AlertDialog.Builder alert = new AlertDialog.Builder(context);
                View mView = LayoutInflater.from(context).inflate(R.layout.order_details_dialog, null);
                MaterialButton btn_cancel = (MaterialButton) mView.findViewById(R.id.btn_cancel);
                MaterialButton btn_okay = (MaterialButton) mView.findViewById(R.id.btn_okay);
                Button cancel = (Button) mView.findViewById(R.id.cancel);

                cancel.setVisibility(View.GONE);

                TextView title = (TextView) mView.findViewById(R.id.message);
                TextView total = (TextView) mView.findViewById(R.id.total);
                ordersRecycler = (RecyclerView) mView.findViewById(R.id.orderList);

                title.setText("DATE: " + recentList.get(position).getDate());
                total.setText("TOTAL: ₱ " + recentList.get(position).getTotal());

                alert.setView(mView);

                GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 1, GridLayoutManager.VERTICAL, false);
                ordersRecycler.setLayoutManager(gridLayoutManager);
                ordersRecycler.setItemAnimator(null);
                orderAdapter = new OrderAdapter(context, orderList);
                ordersRecycler.setAdapter(orderAdapter);

                final AlertDialog alertDialog = alert.create();
                //alertDialog.setCanceledOnTouchOutside(false);

                btn_okay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });

                btn_cancel.setVisibility(View.GONE);
                alertDialog.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return recentList != null ? recentList.size() : 0;
    }

    public static class RecentOrdersViewHolder extends RecyclerView.ViewHolder{
        TextView date, total;

        public RecentOrdersViewHolder(@NonNull View itemView) {
            super(itemView);

            date = itemView.findViewById(R.id.date);
            total = itemView.findViewById(R.id.total);
        }
    }
}