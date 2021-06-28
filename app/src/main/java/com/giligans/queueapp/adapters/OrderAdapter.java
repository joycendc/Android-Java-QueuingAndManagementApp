package com.giligans.queueapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.giligans.queueapp.R;
import com.giligans.queueapp.models.OrderModel;

import java.util.ArrayList;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder>{
    public ArrayList<OrderModel> order;
    Context context;

    public OrderAdapter(Context context, ArrayList<OrderModel> order) {
        this.context = context;
        this.order = order;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new OrderViewHolder(LayoutInflater.from(context).inflate(R.layout.order_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        holder.name.setText(order.get(position).getName());
        holder.qty.setText(order.get(position).getQty());
        holder.total.setText(order.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return order.size();
    }


    public class OrderViewHolder extends RecyclerView.ViewHolder {
    TextView name, qty, total;

    public OrderViewHolder(@NonNull View itemView) {
        super(itemView);
        name = itemView.findViewById(R.id.name);
        qty = itemView.findViewById(R.id.qty);
        total = itemView.findViewById(R.id.total);

    }
}
}
