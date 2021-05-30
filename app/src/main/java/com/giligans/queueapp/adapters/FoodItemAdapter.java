package com.giligans.queueapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
<<<<<<< HEAD
import android.view.inputmethod.InputMethodManager;
=======
>>>>>>> 9e3b33763f3e6ef32080d1b0fffeea1543eb516c
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
<<<<<<< HEAD
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
=======
>>>>>>> 9e3b33763f3e6ef32080d1b0fffeea1543eb516c
import com.giligans.queueapp.R;
import com.giligans.queueapp.fragments.ProducDetailsFragment;
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

    public void updateList(List<FoodModel> foodModelList){
        this.foodModelList = null;
        this.foodModelList = foodModelList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecentlyViewedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecentlyViewedViewHolder(LayoutInflater.from(context).inflate(R.layout.food_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecentlyViewedViewHolder holder, final int position) {
        holder.name.setText(foodModelList.get(position).getName());
        holder.price.setText("₱ " + foodModelList.get(position).getPrice());

        Glide.with(context)
            .load(foodModelList.get(position).getImageUrl())
            .placeholder(R.drawable.ic_menu_24dp)
<<<<<<< HEAD
            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE)
            .error(R.drawable.ic_menu_24dp))
            .dontAnimate()
=======
>>>>>>> 9e3b33763f3e6ef32080d1b0fffeea1543eb516c
            .into(holder.imageView);

        ProducDetailsFragment pdf = new ProducDetailsFragment(
                foodModelList.get(position).getName(),
                foodModelList.get(position).getDescription(),
                foodModelList.get(position).getPrice(),
                foodModelList.get(position).getBigimageurl());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
<<<<<<< HEAD
                InputMethodManager inputMethodManager =(InputMethodManager) context.getSystemService(context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                try {
                    ((FragmentActivity) view.getContext()).getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, pdf)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
=======

                try {
                    ((FragmentActivity) view.getContext()).getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, pdf)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
>>>>>>> 9e3b33763f3e6ef32080d1b0fffeea1543eb516c
                            .addToBackStack(null)
                            .commit();
//                    int size =  ((MainApp) view.getContext()).bottomNav.getMenu().size();
//                    for (int i = 0; i < size; i++) {
//                        ((MainApp) view.getContext()).bottomNav.getMenu().getItem(i).setChecked(false);
//                    }
                }catch(NullPointerException e){
                    Toast.makeText(context,e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }



    @Override
    public int getItemCount() {
        return foodModelList != null ? foodModelList.size() : 0;
    }

    public  static class RecentlyViewedViewHolder extends RecyclerView.ViewHolder{
        TextView name, price;
        LinearLayout bg;
        ImageView imageView;

        public RecentlyViewedViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            name = itemView.findViewById(R.id.product_name);
            price = itemView.findViewById(R.id.price);
            bg = itemView.findViewById(R.id.recently_layout);
        }
    }
}