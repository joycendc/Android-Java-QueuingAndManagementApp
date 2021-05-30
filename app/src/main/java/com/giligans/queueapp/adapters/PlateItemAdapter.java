package com.giligans.queueapp.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.giligans.queueapp.MainApp;
import com.giligans.queueapp.R;
import com.giligans.queueapp.interfaces.TotalClickListener;
import com.giligans.queueapp.models.PlateModel;
import com.google.gson.Gson;

import java.util.ArrayList;

public class PlateItemAdapter extends RecyclerView.Adapter<PlateItemAdapter.PlateViewHolder> {
    Context context;
    ArrayList<PlateModel> plateModel;
    int qty;
    private TotalClickListener totalClickListener;

    public PlateItemAdapter(Context context, TotalClickListener totalClickListener , ArrayList<PlateModel> plateModel) {
        this.context = context;
        this.plateModel = plateModel;
        this.totalClickListener = totalClickListener;
    }

    public void updateList(ArrayList<PlateModel> plateModel){
        this.plateModel = null;
        this.plateModel = plateModel;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PlateViewHolder(LayoutInflater.from(context).inflate(R.layout.plate_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PlateViewHolder holder, final int position) {
        qty = plateModel.get(position).getQty();
        holder.name.setText(plateModel.get(position).getName());
        holder.price.setText("₱ " + plateModel.get(position).getPrice());
        holder.qty.setText(String.valueOf(plateModel.get(position).getQty()));
        holder.total.setText("₱ " + String.valueOf(String.format("%,d", plateModel.get(position).getTotal())));

        Glide.with(context)
            .load(plateModel.get(position).getBigimageurl())
            .placeholder(R.drawable.ic_menu_24dp)
            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE)
            .error(R.drawable.ic_menu_24dp))
            .dontAnimate()
            .into(holder.foodImage);

        holder.inc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                qty = plateModel.get(position).getQty();
                if(qty < 10) {
                    qty++;
                    plateModel.get(position).setQty(qty);
                    plateModel.get(position).setTotal(Integer.parseInt(plateModel.get(position).getPrice()) * plateModel.get(position).getQty());
                    holder.total.setText("₱ " + String.format("%,d", plateModel.get(position).getTotal()));
                    holder.qty.setText(String.valueOf(qty));
                    int sum = 0;
                    for (PlateModel p : plateModel) {
                        sum += p.getTotal();
                    }
                    totalClickListener.onItemClick("₱ " + String.format("%,d", sum));
                }
            }
        });

        holder.dec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                qty = plateModel.get(position).getQty();
                if(qty > 1){
                    qty--;
                    plateModel.get(position).setQty(qty);
                    plateModel.get(position).setTotal(Integer.parseInt(plateModel.get(position).getPrice()) * plateModel.get(position).getQty());
                    holder.total.setText("₱ " + String.format("%,d", plateModel.get(position).getTotal())
                    );
                    holder.qty.setText(String.valueOf(qty));
                    int sum = 0;
                    for(PlateModel p : plateModel){
                        sum += p.getTotal();
                    }
                    totalClickListener.onItemClick("₱ " + String.format("%,d", sum));
                }

            }
        });
    }

    public void removeItem(int position) {
        plateModel.remove(position);
        updateList(plateModel);

        SharedPreferences sp = context.getSharedPreferences("plate_list", Context.MODE_PRIVATE);
        String json = sp.getString("orderlist", null);
        SharedPreferences.Editor editor = context.getSharedPreferences("plate_list", Context.MODE_PRIVATE).edit();
        Gson gson = new Gson();
        json = gson.toJson(plateModel);
        editor.putString("orderlist", json);
        editor.commit();

        notifyDataSetChanged();
    }

    public PlateModel getEntity(int adapterPosition) {
        return plateModel.get(adapterPosition);
    }

    public void undoDelete(PlateModel entity, int position) {
        plateModel.add(position, entity);
        updateList(plateModel);

        SharedPreferences sp = context.getSharedPreferences("plate_list", Context.MODE_PRIVATE);
        String json = sp.getString("orderlist", null);
        SharedPreferences.Editor editor = context.getSharedPreferences("plate_list", Context.MODE_PRIVATE).edit();
        Gson gson = new Gson();
        json = gson.toJson(plateModel);
        editor.putString("orderlist", json);
        editor.commit();

        ((MainApp)context).setBadgeCount(((MainApp)context).getItemCount());
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return plateModel.size();
    }

    public  static class PlateViewHolder extends RecyclerView.ViewHolder{
        TextView name, price, qty, total;
        ImageButton inc, dec;
        ImageView foodImage;

        public PlateViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.foodName);
            price = itemView.findViewById(R.id.foodPrice);
            qty = itemView.findViewById(R.id.foodQty);
            total = itemView.findViewById(R.id.foodTotal);
            inc = itemView.findViewById(R.id.increase);
            dec = itemView.findViewById(R.id.decrease);
            foodImage = itemView.findViewById(R.id.foodImage);

        }
    }
}