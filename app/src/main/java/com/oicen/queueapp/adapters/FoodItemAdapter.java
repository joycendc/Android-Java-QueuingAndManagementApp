package com.oicen.queueapp.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.oicen.queueapp.R;
import com.oicen.queueapp.fragments.ProducDetailsFragment;
import com.oicen.queueapp.models.FoodModel;
import com.oicen.queueapp.utils.GlideApp;

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

        GlideApp.with(context)
            .load(foodModelList.get(position).getImageUrl())
            .placeholder(R.drawable.ic_menu_24dp)
            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE)
            .error(R.drawable.ic_fastfood_24dp))
            .dontAnimate()
            .into(holder.imageView);

        holder.imageView.setElevation(10);

        ProducDetailsFragment pdf = new ProducDetailsFragment(
                foodModelList.get(position).getId(),
                foodModelList.get(position).getName(),
                foodModelList.get(position).getDescription(),
                foodModelList.get(position).getPrice(),
                foodModelList.get(position).getBigimageurl());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager inputMethodManager =(InputMethodManager) context.getSystemService(context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                try {
                    ((FragmentActivity) view.getContext()).getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, pdf)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .addToBackStack("home")
                            .commit();
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