package com.oicen.queueapp.fragments;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.oicen.queueapp.R;
import com.oicen.queueapp.activities.MainApp;
import com.oicen.queueapp.adapters.FoodItemAdapter;
import com.oicen.queueapp.models.FoodModel;
import com.oicen.queueapp.utils.ApiHelper;
import com.oicen.queueapp.utils.DBContract;
import com.oicen.queueapp.utils.DBHelper;
import com.oicen.queueapp.utils.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.oicen.queueapp.BuildConfig.HOST;

public class TabCategoryFragment extends Fragment {
    String ITEM_URL = HOST + ApiHelper.ITEM_URL;
    public RecyclerView foodListRecycler;
    public FoodItemAdapter foodListAdapter;
    public List<FoodModel> foodListList;
    FragmentManager fragmentManager;
    int catId;
    Context context;
    DBHelper dbHelper;
    SQLiteDatabase db;
    ShimmerFrameLayout shimmerFrameLayout;

    public TabCategoryFragment() { }

    public TabCategoryFragment(int catId) {
        this.catId = catId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getActivity();
        View view = inflater.inflate(R.layout.fragment_tab_category, container, false);
        foodListRecycler = (RecyclerView) view.findViewById(R.id.foods);

        shimmerFrameLayout = (ShimmerFrameLayout) view.findViewById(R.id.shimmerLayout);

        if(catId > 0) ITEM_URL = HOST + ApiHelper.ITEM_URLCAT + catId;
        dbHelper = new DBHelper(context);
        shimmerFrameLayout.startShimmer();
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        loadItems();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setRecentlyViewedRecycler(foodListList);
        fragmentManager = getActivity().getSupportFragmentManager();
    }

    @Override
    public void onViewStateRestored(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        shimmerFrameLayout.startShimmer();
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    void readFromLocalDB(){
        db = dbHelper.getReadableDatabase();
        foodListList = new ArrayList<FoodModel>();
        Cursor cursor = dbHelper.readItemsFromLocalDB(db, catId);
        if(cursor != null){
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex(DBContract.ID));
                String name = cursor.getString(cursor.getColumnIndex(DBContract.NAME));
                String description = cursor.getString(cursor.getColumnIndex(DBContract.DESC));
                String price = cursor.getString(cursor.getColumnIndex(DBContract.PRICE));
                foodListList.add(new FoodModel(id, name, description, price, "", ""));
            }
        }else{
            Toast.makeText(context, "PLEASE CONNECT TO OUR WIFI", Toast.LENGTH_SHORT).show();
        }

        setRecentlyViewedRecycler(foodListList);
        shimmerFrameLayout.stopShimmer();
        shimmerFrameLayout.setVisibility(View.GONE);
        foodListRecycler.setVisibility(View.VISIBLE);
        cursor.close();
        db.close();
    }

    private void loadItems(){
        if(!((MainApp)getActivity()).checkNetworkConnection()) {
            readFromLocalDB();
        }else {
            foodListList = new ArrayList<FoodModel>();
            StringRequest stringRequest = new StringRequest(Request.Method.GET, ITEM_URL,
                new Response.Listener<String>(){
                    @Override
                    public void onResponse(String response){
                        try {
                            JSONArray items = new JSONArray(response);
                            for(int i = 0; i < items.length(); i++){
                                JSONObject itemObject = items.getJSONObject(i);

                                int id = itemObject.getInt("id");
                                String name = itemObject.getString("name");
                                String description = itemObject.getString("description");
                                String price = itemObject.getString("price");
                                int cat_id = itemObject.getInt("id");
                                String url = HOST + "images/" + itemObject.getString("url");

                                foodListList.add(new FoodModel(id, name, description, price, url, url));
                            }
                            setRecentlyViewedRecycler(foodListList);
                            shimmerFrameLayout.stopShimmer();
                            shimmerFrameLayout.setVisibility(View.GONE);
                            foodListRecycler.setVisibility(View.VISIBLE);
                        } catch (JSONException e) {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error){
                        readFromLocalDB();
                    }
                }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put(ApiHelper.KEY_COOKIE, ApiHelper.VALUE_CONTENT);
                    return headers;
                }
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0, -1,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(getActivity().getApplicationContext()).addToRequestQueue(stringRequest);

        }
    }

    private void setRecentlyViewedRecycler(List<FoodModel> foodModelDataList) {
        foodListRecycler.setHasFixedSize(true);
        foodListRecycler.setItemAnimator(new DefaultItemAnimator());
        foodListAdapter = new FoodItemAdapter(context, fragmentManager, foodModelDataList);
        foodListRecycler.setAdapter(foodListAdapter);
    }
}
