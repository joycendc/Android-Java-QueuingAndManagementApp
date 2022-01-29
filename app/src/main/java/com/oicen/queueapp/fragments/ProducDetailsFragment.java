package com.oicen.queueapp.fragments;

import static com.oicen.queueapp.BuildConfig.HOST;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.oicen.queueapp.R;
import com.oicen.queueapp.activities.MainApp;
import com.oicen.queueapp.models.PlateModel;
import com.oicen.queueapp.utils.ApiHelper;
import com.oicen.queueapp.utils.VolleySingleton;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProducDetailsFragment extends Fragment {
    final String FAVE = HOST + ApiHelper.FAVE;
    ImageView back, food;
    CardView img;
    String name;
    String description;
    String price, foodImage;
    TextView proName, proPrice, proDesc, proQty;
    public MaterialButton addToPlate;
    ArrayList<PlateModel> plateModel;
    int qty;
    ImageButton inc, dec, addFav;
    boolean isFave = false;
    int item_id;

    public ProducDetailsFragment() {}

    public ProducDetailsFragment(int id, String name, String desc, String price, String foodImage) {
        this.item_id = id;
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
        addFav = (ImageButton) view.findViewById(R.id.addFav);

        proName.setText(this.name);
        proPrice.setText("₱ " + this.price);
        proDesc.setText(this.description);

        Glide.with(getContext())
                .load(foodImage)
                .placeholder(R.drawable.ic_fastfood_24dp)
                .into(food);

        food.setElevation(10);

        if(((MainApp)getActivity()).connectivity) addToPlate.setEnabled(true);




            inc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(qty < 10) qty++;
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

        addFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isFave) {
                    isFave = true;
                    setFav("add");
                }else{
                    isFave = false;
                    setFav("remove");
                }
                addFav.setImageResource(isFave ? R.drawable.ic_favorite_24dp : R.drawable.ic_favorite_border_24dp);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainApp)getActivity()).onBackPressed();
            }
        });

        addToPlate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(((MainApp)getActivity()).isPaid) {
                    ((MainApp)getActivity()).showMessage("You can' t add orders now because your order is being served!");
                }else {
                    SharedPreferences sp = getActivity().getSharedPreferences("plate_list", Context.MODE_PRIVATE);
                    String json = sp.getString("orderlist", null);
                    boolean existing = false;

                    if (json != null) {
                        Gson gson = new Gson();
                        Type type = new TypeToken<ArrayList<PlateModel>>() {
                        }.getType();
                        ArrayList<PlateModel> newList = new ArrayList<PlateModel>();
                        newList = gson.fromJson(json, type);
                        for (int i = 0; i < newList.size(); i++) {
                            if (newList.get(i).getName().equals(name)) {
                                existing = true;
                                newList.get(i).setQty(qty += newList.get(i).getQty());
                                newList.get(i).setTotal(newList.get(i).getQty() * Integer.parseInt(newList.get(i).getPrice()));
                                Toast.makeText(getActivity().getApplicationContext(), "You Added Another" + name + " To Plate", Toast.LENGTH_SHORT).show();
                            }
                            plateModel.add(new PlateModel(newList.get(i).getName(), newList.get(i).getPrice(), newList.get(i).getQty(), newList.get(i).getTotal(), newList.get(i).getBigimageurl()));
                        }
                    }

                    if (!existing) {
                        plateModel.add(new PlateModel(name, price, qty, (Integer.parseInt(price) * qty), foodImage));
                        Toast.makeText(getActivity().getApplicationContext(), name + " added", Toast.LENGTH_SHORT).show();
                    }
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences("plate_list", Context.MODE_PRIVATE).edit();
                    Gson gson = new Gson();
                    json = gson.toJson(plateModel);
                    editor.putString("orderlist", json).commit();
                    plateModel = new ArrayList<PlateModel>();

                    ((MainApp) getActivity()).setBadgeCount(((MainApp) getActivity()).getItemCount());
                }
            }
        });
        checkFav();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

     }

    void setQty(){
        proQty.setText(qty+"");
    }

    void checkFav(){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, FAVE,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (!obj.getBoolean("error")) {
                            isFave = obj.getBoolean("status");
                        }
                    }catch (Exception e){
                        isFave = false;
                    }
                    addFav.setImageResource(isFave ? R.drawable.ic_favorite_24dp : R.drawable.ic_favorite_border_24dp);
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    isFave = false;
                }
            }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                SharedPreferences sharedPreferences = getContext().getSharedPreferences("login", Context.MODE_PRIVATE);
                String id = sharedPreferences.getString("keyid", null);
                params.put("customer_id", id);
                params.put("item_id", String.valueOf(item_id));
                params.put("fave", "check");
                return params;
            }
        };
        VolleySingleton.getInstance(getContext()).addToRequestQueue(stringRequest);

    }

    void setFav(String mode){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, FAVE,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (!obj.getBoolean("error")) {
                            String status = obj.getString("status");

                            Toast.makeText(getActivity().getApplicationContext(), status, Toast.LENGTH_SHORT).show();
                         }
                    }catch (Exception e){
                        isFave = false;
                        e.printStackTrace();
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                SharedPreferences sharedPreferences = getContext().getSharedPreferences("login", Context.MODE_PRIVATE);
                String id = sharedPreferences.getString("keyid", null);
                params.put("customer_id", id);
                params.put("item_id", String.valueOf(item_id));
                params.put("fave", mode);
                return params;
            }
        };
        VolleySingleton.getInstance(getContext()).addToRequestQueue(stringRequest);
    }

}