package com.giligans.queueapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.giligans.queueapp.MainApp;
import com.giligans.queueapp.R;
import com.giligans.queueapp.models.PlateModel;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class ProducDetailsFragment extends Fragment {
    ImageView back, food;
    CardView img;
    String name;
    String description;
    String price, foodImage;
    TextView proName, proPrice, proDesc, proQty;
    public MaterialButton addToPlate;
    ArrayList<PlateModel> plateModel;
    int qty;
    ImageButton inc, dec;

    public ProducDetailsFragment() {}

    public ProducDetailsFragment(String name, String desc, String price, String foodImage) {
        this.name = name;
        this.description = desc;
        this.price = price;
        this.foodImage = foodImage;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        qty = 1;
        plateModel = new ArrayList<PlateModel>();
        View view = inflater.inflate(R.layout.fragment_product_details, container, false);
        proName = (TextView) view.findViewById(R.id.productName);
        proDesc = (TextView) view.findViewById(R.id.prodDesc);
        proPrice = (TextView) view.findViewById(R.id.prodPrice);
        proQty = (TextView) view.findViewById(R.id.qty);
        back = (ImageView) view.findViewById(R.id.back2);
        food = (ImageView) view.findViewById(R.id.big_image);
        addToPlate = (MaterialButton) view.findViewById(R.id.addToPlate);
        inc = (ImageButton) view.findViewById(R.id.increase);
        dec = (ImageButton) view.findViewById(R.id.decrease);

        proName.setText(this.name);
        proPrice.setText("₱ " + this.price);
        proDesc.setText(this.description);

        Glide.with(getContext())
                .load(foodImage)
                .placeholder(R.drawable.ic_fastfood_24dp)
                .into(food);

        if(((MainApp)getActivity()).connectivity){ addToPlate.setEnabled(true); }
        if(((MainApp)getActivity()).mTimerRunning){ addToPlate.setEnabled(false); }

        inc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                qty++;
                setQty();
            }
        });

        dec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(qty > 1) qty--;
                setQty();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //((MainApp) getContext()).showBottomNav();
//                if(((MainApp)getActivity()).bottomNav.getSelectedItemId() == R.id.navigation_dashboard){
//                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
//                    ft.replace(R.id.fragment_container, new CategoriesFragment()).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
//                }else {
//                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
//                    ft.replace(R.id.fragment_container, new HomeFragment()).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
//                }
                ((MainApp)getActivity()).setFragment(((MainApp)getActivity()).bottomNav.getSelectedItemId());
            }
        });

        addToPlate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sp = getActivity().getSharedPreferences("plate_list", Context.MODE_PRIVATE);
                String json = sp.getString("orderlist", null);
                boolean existing = false;

                if(json != null){
                    Gson gson = new Gson();
                    Type type = new TypeToken<ArrayList<PlateModel>>() {}.getType();
                    ArrayList<PlateModel> newList = new ArrayList<PlateModel>();
                    newList = gson.fromJson(json, type);
                    for(int i = 0; i < newList.size(); i++){
                        if(newList.get(i).getName().equals(name)){
                            existing = true;
                            newList.get(i).setQty(qty+= newList.get(i).getQty());
                            newList.get(i).setTotal(newList.get(i).getQty() * Integer.parseInt(newList.get(i).getPrice()));
                            Toast.makeText(getActivity().getApplicationContext(),"You Added Another" + name + " To Plate", Toast.LENGTH_SHORT).show();
                        }
                        newList.get(i).setBigimageurl(foodImage);
                        plateModel.add(new PlateModel(newList.get(i).getName(), newList.get(i).getPrice(), newList.get(i).getQty(), newList.get(i).getTotal(), newList.get(i).getBigimageurl()));
                    }
                }

                if(!existing) {
                    plateModel.add(new PlateModel(name, price, qty, (Integer.parseInt(price)*qty), foodImage));
                    Toast.makeText(getActivity().getApplicationContext(),name + " added", Toast.LENGTH_SHORT).show();
                }
                SharedPreferences.Editor editor = getActivity().getSharedPreferences("plate_list", Context.MODE_PRIVATE).edit();
                Gson gson = new Gson();
                json = gson.toJson(plateModel);
                editor.putString("orderlist", json).commit();
                plateModel = new ArrayList<PlateModel>();

                ((MainApp)getActivity()).setBadgeCount(((MainApp)getActivity()).getItemCount());
            }
        });
        return view;
    }

    void setQty(){
        proQty.setText(qty+"");
    }
}