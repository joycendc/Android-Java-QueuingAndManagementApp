package com.giligans.queueapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.giligans.queueapp.R;
import com.giligans.queueapp.models.PlateModel;

import java.util.List;

import static java.lang.Integer.parseInt;

public class PlateItemAdapter extends RecyclerView.Adapter<PlateItemAdapter.PlateViewHolder> {
    Context context;
    List<PlateModel> plateModel;

    public PlateItemAdapter(Context context, List<PlateModel> plateModel) {
        this.context = context;
        this.plateModel = plateModel;
    }

    @NonNull
    @Override
    public PlateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.plate_item, parent, false);
        return new PlateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlateViewHolder holder, final int position) {
        holder.name.setText(plateModel.get(position).getName());
        holder.price.setText(plateModel.get(position).getPrice());
        holder.qty.setText(Integer.toString(plateModel.get(position).getQty()));
        holder.total.setText(Integer.toString(plateModel.get(position).getQty() * parseInt(plateModel.get(position).getPrice())));

    }

    @Override
    public int getItemCount() {
        return plateModel.size();
    }

    public  static class PlateViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        TextView price;
        TextView qty;
        TextView total;

        public PlateViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.foodName);
            price = itemView.findViewById(R.id.foodTotal);
            qty = itemView.findViewById(R.id.foodQty);
            total = itemView.findViewById(R.id.foodTotal);
        }
    }
}