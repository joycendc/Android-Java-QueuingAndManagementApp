package com.giligans.queueapp.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.giligans.queueapp.MyDiffUtilCallBack;
import com.giligans.queueapp.R;
import com.giligans.queueapp.models.CustomerModel;

import java.util.ArrayList;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder> {
    public ArrayList<CustomerModel> customer;
    Context context;

    public CustomerAdapter(Context context, ArrayList<CustomerModel> customer) {
        this.context = context;
        this.customer = customer;
    }

    public void compare(ArrayList<CustomerModel> newData){
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new MyDiffUtilCallBack(newData, this.customer));

        this.customer = new ArrayList<CustomerModel>();
        this.customer = newData;
        diffResult.dispatchUpdatesTo(this);
        //notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CustomerViewHolder(LayoutInflater.from(context).inflate(R.layout.customer, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("login", Context.MODE_PRIVATE);
        final String keyname = sharedPreferences.getString("keyfname", null) + " " + sharedPreferences.getString("keylname", null);
        holder.name.setText("CUSTOMER " + (position + 1));
        holder.id.setText(customer.get(position).getId());
        if (position == 0) {
            holder.card.setCardBackgroundColor(Color.parseColor("#f70d1a"));
            if (customer.get(position).getName().equals(keyname)) {
                holder.name.setText("You (" + keyname + ")");
                holder.card.setCardBackgroundColor(Color.parseColor("#ff8c00"));
                holder.name.setTextColor(Color.parseColor("#ffffffff"));
                holder.id.setTextColor(Color.parseColor("#ffffffff"));
                holder.status.setTextColor(Color.parseColor("#ffffffff"));
                holder.image.setColorFilter(R.color.white, android.graphics.PorterDuff.Mode.MULTIPLY);
            }
            holder.status.setText("NOW SERVING");
        } else {
            if (customer.get(position).getName().equals(keyname)) {
                holder.name.setText("You (" + keyname + ")");
                holder.card.setCardBackgroundColor(Color.parseColor("#009d00"));
                holder.name.setTextColor(Color.parseColor("#ffffffff"));
                holder.id.setTextColor(Color.parseColor("#ffffffff"));
                holder.status.setTextColor(Color.parseColor("#ffffffff"));
                holder.image.setColorFilter(R.color.white, android.graphics.PorterDuff.Mode.MULTIPLY);
            }else{
                holder.card.setCardBackgroundColor(Color.parseColor("#ffffffff"));
                holder.name.setTextColor(Color.parseColor("#ff000000"));
                holder.id.setTextColor(Color.parseColor("#ff000000"));
                holder.status.setTextColor(Color.parseColor("#ff000000"));
                holder.image.setColorFilter(R.color.black, android.graphics.PorterDuff.Mode.MULTIPLY);
            }
            holder.status.setText("IN LINE");
        }
    }

    @Override
    public int getItemCount() {
        return customer != null ? customer.size() : 0;
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