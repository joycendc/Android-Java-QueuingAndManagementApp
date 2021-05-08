package com.giligans.queueapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;


import com.giligans.queueapp.fragments.ProducDetailsFragment;

import com.giligans.queueapp.R;
import com.giligans.queueapp.models.FoodModel;

import java.util.List;


public class FoodItemAdapter extends RecyclerView.Adapter<FoodItemAdapter.RecentlyViewedViewHolder> {
    Context context;
    List<FoodModel> foodModelList;
    FragmentManager fragmentManager;

    public FoodItemAdapter(Context context, FragmentManager fragmentManager, List<FoodModel> foodModelList) {
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.foodModelList = foodModelList;
    }

    @NonNull
    @Override
    public RecentlyViewedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecentlyViewedViewHolder(LayoutInflater.from(context).inflate(R.layout.food_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecentlyViewedViewHolder holder, final int position) {
        holder.name.setText(foodModelList.get(position).getName());
        holder.description.setText(foodModelList.get(position).getDescription());
        holder.price.setText(foodModelList.get(position).getPrice());
        //holder.bg.setBackgroundResource(foodModelList.get(position).getImageUrl());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction ft = fragmentManager.beginTransaction();
                ft.replace(R.id.fragment_container,
                    new ProducDetailsFragment(
                            foodModelList.get(position).getName(),
                            foodModelList.get(position).getDescription(),
                            foodModelList.get(position).getPrice()), null);
                ft.addToBackStack(null);
                ft.commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return foodModelList.size();
    }

    public  static class RecentlyViewedViewHolder extends RecyclerView.ViewHolder{
        TextView name, description, price;
        CardView bg;

        public RecentlyViewedViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.product_name);
            description = itemView.findViewById(R.id.description);
            price = itemView.findViewById(R.id.price);
            bg = itemView.findViewById(R.id.recently_layout);

        }
    }
}