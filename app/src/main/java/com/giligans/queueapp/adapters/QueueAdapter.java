package com.giligans.queueapp.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.giligans.queueapp.utils.QueueDiffUtilCallBack;
import com.giligans.queueapp.R;
import com.giligans.queueapp.models.QueueModel;

import java.util.ArrayList;

public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.CustomerViewHolder> {
    public ArrayList<QueueModel> customer;
    Context context;

    public QueueAdapter(Context context, ArrayList<QueueModel> customer) {
        this.context = context;
        this.customer = customer;
    }

    public void update(ArrayList<QueueModel> newData){
        QueueAdapter self = this;
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new QueueDiffUtilCallBack(newData, customer));
                customer.clear();
                diffResult.dispatchUpdatesTo(self);
                customer.addAll(newData);
                notifyDataSetChanged();
            }
        }, 500);

    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CustomerViewHolder(LayoutInflater.from(context).inflate(R.layout.queue_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("login", Context.MODE_PRIVATE);
        String keyname = sharedPreferences.getString("keyfname", null) + " " + sharedPreferences.getString("keylname", null);
        holder.name.setText("CUSTOMER " + (position + 1));
        holder.id.setText(customer.get(position).getQueueId());
        if (position == 0) {
            holder.card.setCardBackgroundColor(Color.parseColor("#f70d1a"));
            if (customer.get(position).getName().equals(keyname)) {
                holder.name.setText("You (" + keyname.toUpperCase() + ")");
                holder.card.setCardBackgroundColor(Color.parseColor("#ff8c00"));
                holder.name.setTextColor(Color.parseColor("#ffffffff"));
                holder.id.setTextColor(Color.parseColor("#ffffffff"));
                holder.status.setTextColor(Color.parseColor("#ffffffff"));
                holder.image.setColorFilter(context.getResources().getColor(R.color.white));
            }
            holder.status.setText("NOW SERVING");
        } else {
            if (customer.get(position).getName().equals(keyname)) {
                holder.name.setText("You ( " + keyname.toUpperCase() + " )");
                holder.card.setCardBackgroundColor(Color.parseColor("#009d00"));
                holder.name.setTextColor(Color.parseColor("#ffffffff"));
                holder.id.setTextColor(Color.parseColor("#ffffffff"));
                holder.status.setTextColor(Color.parseColor("#ffffffff"));
                holder.image.setColorFilter(context.getResources().getColor(R.color.white));
            }else{
                holder.card.setCardBackgroundColor(Color.parseColor("#ffffffff"));
                holder.name.setTextColor(Color.parseColor("#ff000000"));
                holder.id.setTextColor(Color.parseColor("#ff000000"));
                holder.status.setTextColor(Color.parseColor("#ff000000"));
                holder.image.setColorFilter(context.getResources().getColor(R.color.black));
            }
            holder.status.setText("IN LINE");
        }
    }

    @Override
    public int getItemCount() {
        return customer.size();
    }

    public class CustomerViewHolder extends RecyclerView.ViewHolder {
        TextView name, id, status;
        CardView card;
        ImageView image;

        public CustomerViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.customerName);
            id = itemView.findViewById(R.id.customerNumber);
            status = itemView.findViewById(R.id.status);
            card = itemView.findViewById(R.id.row);
            image = itemView.findViewById(R.id.customerImage);
        }
    }
}