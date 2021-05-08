package com.giligans.queueapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.giligans.queueapp.R;
import com.giligans.queueapp.models.CustomerModel;
import java.util.ArrayList;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder> {
    ArrayList<CustomerModel> customer;
    Context context;

    public CustomerAdapter(Context context, ArrayList<CustomerModel> customer) {
        this.context = context;
        this.customer = customer;
    }

    public void updateList(ArrayList<CustomerModel> customer){
        this.customer.clear();
        this.customer.addAll(customer);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.customer, parent, false);

        return new CustomerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        //holder.setIsRecyclable(true);

        if(position == 0){
            holder.itemView.setBackgroundColor(Color.parseColor("#f70d1a"));
        }
        if(customer.get(position).getId().equals(customer.get(getItemCount()-2).getId())){
            holder.itemView.setBackgroundColor(Color.parseColor("#006200"));
        }
        holder.name.setText(customer.get(position).getName());
        holder.number.setText(customer.get(position).getQueueNumber());

    }

    @Override
    public int getItemCount() {
        return  customer.size();
    }
//
//    public void resetData(ArrayList<CustomerModel> customer){
//        this.customer = customer;
//        this.notifyDataSetChanged();
//    }

    public class CustomerViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView number;


        public CustomerViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.customerName);
            number = itemView.findViewById(R.id.customerNumber);

        }
    }
}