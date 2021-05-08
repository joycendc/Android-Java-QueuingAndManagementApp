package com.giligans.queueapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class TabCategoryFragment extends Fragment {
    String ITEM_URL = "http://192.168.254.152/fetchitems.php";
    RecyclerView foodListRecycler;
    FoodItemAdapter foodListAdapter;
    List<FoodModel> foodListList;
    FragmentManager fragmentManager;
    int catId;
    public TabCategoryFragment() {
        // Required empty public constructor
    }

    public TabCategoryFragment(int catId) {
        this.catId = catId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tab_category, container, false);
        foodListRecycler = (RecyclerView) view.findViewById(R.id.foods);


        ITEM_URL = "http://192.168.254.152/fetchitemscat.php?id=" + catId;

        loadItems();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setRecentlyViewedRecycler(foodListList);
        fragmentManager = getActivity().getSupportFragmentManager();
    }

    private void loadItems(){
        foodListList = new ArrayList<>();
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
                            foodListList.add(new FoodModel(name, description, price, 0, 0));
                        }
                        setRecentlyViewedRecycler(foodListList);
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
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity().getApplicationContext(),  Utility.calculateNoOfColumns(getActivity().getApplicationContext(), 150));
        foodListRecycler.setLayoutManager(gridLayoutManager);
        foodListAdapter = new FoodItemAdapter(getActivity().getApplicationContext(),fragmentManager, foodModelDataList);
        foodListRecycler.setAdapter(foodListAdapter);
    }
}
