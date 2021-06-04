package com.giligans.queueapp.fragments;

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

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.giligans.queueapp.utils.DBContract;
import com.giligans.queueapp.utils.DBHelper;
import com.giligans.queueapp.activities.MainApp;
import com.giligans.queueapp.R;
import com.giligans.queueapp.utils.VolleySingleton;
import com.giligans.queueapp.adapters.FoodItemAdapter;
import com.giligans.queueapp.models.FoodModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.giligans.queueapp.BuildConfig.HOST;

public class TabCategoryFragment extends Fragment {
    String ITEM_URL = HOST + "fetchitems.php";
    public RecyclerView foodListRecycler;
    public FoodItemAdapter foodListAdapter;
    public List<FoodModel> foodListList;
    FragmentManager fragmentManager;
    int catId;
    Context context;
    DBHelper dbHelper;
    SQLiteDatabase db;

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
        if(catId > 0) ITEM_URL = HOST + "fetchitemscat.php?id=" + catId;
        dbHelper = new DBHelper(context);
        loadItems();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setRecentlyViewedRecycler(foodListList);
        fragmentManager = getActivity().getSupportFragmentManager();
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
                                String preptime = itemObject.getString("prep_time");
                                int cat_id = itemObject.getInt("id");
                                String url = HOST + "images/" + itemObject.getString("url");

                                foodListList.add(new FoodModel(id, name, description, price, url, url));
                            }
                            setRecentlyViewedRecycler(foodListList);
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
                });
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
