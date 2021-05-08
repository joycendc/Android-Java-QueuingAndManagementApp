package com.giligans.queueapp.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.giligans.queueapp.R;
import com.giligans.queueapp.Utility;
import com.giligans.queueapp.adapters.FoodItemAdapter;
import com.giligans.queueapp.models.FoodModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    String QUERY_URL = "http://192.168.254.152/fetchitems.php";
    final String ITEM_URL = "http://192.168.254.152/fetchitems.php";
    RecyclerView favoriteFoodsRecycler;
    FoodItemAdapter foodItemAdapter;
    List<FoodModel> foodModelList;
    EditText editText;
    FragmentManager fragmentManager;
    TextView alltime;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        favoriteFoodsRecycler = (RecyclerView) view.findViewById(R.id.recently_item);
        editText = (EditText) view.findViewById(R.id.editText);
        alltime = (TextView) view.findViewById(R.id.alltime);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() == 0) {
                    alltime.setVisibility(View.VISIBLE);
                } else {
                    alltime.setVisibility(View.GONE);
                }
                QUERY_URL = "http://192.168.254.152/searchitems.php?query=" + s.toString();
                loadItems(QUERY_URL);

            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        loadItems();
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(isAdded()) {
            setRecentlyViewedRecycler(foodModelList);
            fragmentManager = getActivity().getSupportFragmentManager();
        }
    }

    private void loadItems(){
        foodModelList = new ArrayList<>();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, ITEM_URL,
            new Response.Listener<String>(){
                @Override
                public void onResponse(String response){
                    try {
                        JSONArray items = new JSONArray(response);
                        for(int i = 0; i < items.length(); i++){
                            JSONObject itemObject = items.getJSONObject(i);

                            String name = itemObject.getString("name");
                            String description = itemObject.getString("description");
                            String price = itemObject.getString("price");
                            foodModelList.add(new FoodModel(name, description, price, 0, 0));
                        }
                        setRecentlyViewedRecycler(foodModelList);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error){
                    Toast.makeText(getActivity().getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        Volley.newRequestQueue(getActivity().getApplicationContext()).add(stringRequest);
}

    private void loadItems(String query){
        foodModelList = new ArrayList<>();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, QUERY_URL,
                new Response.Listener<String>(){
                    @Override
                    public void onResponse(String response){
                        try {
                            JSONArray items = new JSONArray(response);
                            for(int i = 0; i < items.length(); i++){
                                JSONObject itemObject = items.getJSONObject(i);

                                String name = itemObject.getString("name");
                                String description = itemObject.getString("description");
                                String price = itemObject.getString("price");
                                foodModelList.add(new FoodModel(name, description, price, 0, 0));
                            }
                            setRecentlyViewedRecycler(foodModelList);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error){
                        Toast.makeText(getActivity().getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        Volley.newRequestQueue(getActivity().getApplicationContext()).add(stringRequest);
    }

    private void setRecentlyViewedRecycler(List<FoodModel> foodModelDataList) {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),  Utility.calculateNoOfColumns(getActivity().getApplicationContext(), 150));
        favoriteFoodsRecycler.setLayoutManager(gridLayoutManager);
        foodItemAdapter = new FoodItemAdapter(getContext(),fragmentManager, foodModelDataList);
        favoriteFoodsRecycler.setAdapter(foodItemAdapter);
    }

}