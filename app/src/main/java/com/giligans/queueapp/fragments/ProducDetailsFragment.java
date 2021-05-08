package com.giligans.queueapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.cardview.widget.CardView;

import com.giligans.queueapp.MainApp;
import com.giligans.queueapp.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.giligans.queueapp.models.PlateModel;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class ProducDetailsFragment extends Fragment {
    ImageView back;
    CardView img;
    String name;
    String description;
    String price;
    TextView proName, proPrice, proDesc;
    Button addToPlate;
    ArrayList<PlateModel> plateModel;

    public ProducDetailsFragment() {}

    public ProducDetailsFragment(String name, String desc, String price) {
        this.name = name;
        this.description = desc;
        this.price = price;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        plateModel = new ArrayList<PlateModel>();
        View view = inflater.inflate(R.layout.fragment_product_details, container, false);
        proName = (TextView) view.findViewById(R.id.productName);
        proDesc = (TextView) view.findViewById(R.id.prodDesc);
        proPrice = (TextView) view.findViewById(R.id.prodPrice);
        back = (ImageView) view.findViewById(R.id.back2);
        addToPlate = (Button) view.findViewById(R.id.addToPlate);

        proName.setText(this.name);
        proPrice.setText(this.price);
        proDesc.setText(this.description);

        if(((MainApp)getActivity()).mTimerRunning){
            addToPlate.setEnabled(false);
        }

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction ft =  getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, new HomeFragment());
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.commit();
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
                            int qty = newList.get(i).getQty();
                            newList.get(i).setQty(qty+=1);
                            Toast.makeText(getActivity().getApplicationContext(),"You Added Another" + name + " To Plate", Toast.LENGTH_SHORT).show();
                        }
                        plateModel.add(new PlateModel(newList.get(i).getName(), newList.get(i).getPrice(), newList.get(i).getQty()));
                    }
                }

                if(!existing) {
                    plateModel.add(new PlateModel(name, price, 1));
                    Toast.makeText(getActivity().getApplicationContext(),name + " added", Toast.LENGTH_SHORT).show();
                }
                SharedPreferences.Editor editor = getActivity().getSharedPreferences("plate_list", Context.MODE_PRIVATE).edit();
                Gson gson = new Gson();
                json = gson.toJson(plateModel);
                editor.putString("orderlist", json);
                editor.commit();
                plateModel = new ArrayList<PlateModel>();

                ((MainApp)getActivity()).setBadgeCount(((MainApp)getActivity()).getItemCount());
            }
        });
        // Inflate the layout for this fragment
        return view;
    }
}