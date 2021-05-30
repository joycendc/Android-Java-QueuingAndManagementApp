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
<<<<<<< HEAD
=======
import com.android.volley.toolbox.Volley;
>>>>>>> 9e3b33763f3e6ef32080d1b0fffeea1543eb516c
import com.giligans.queueapp.DBContract;
import com.giligans.queueapp.DBHelper;
import com.giligans.queueapp.MainApp;
import com.giligans.queueapp.R;
<<<<<<< HEAD
import com.giligans.queueapp.VolleySingelton;
=======
>>>>>>> 9e3b33763f3e6ef32080d1b0fffeea1543eb516c
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
                String name = cursor.getString(cursor.getColumnIndex(DBContract.NAME));
                String description = cursor.getString(cursor.getColumnIndex(DBContract.DESC));
                String price = cursor.getString(cursor.getColumnIndex(DBContract.PRICE));
                foodListList.add(new FoodModel(name, description, price, "", ""));
            }
        }else{
            Toast.makeText(context, "PLEASE CONNECT TO OUR WIFI", Toast.LENGTH_SHORT).show();
        }
        setRecentlyViewedRecycler(foodListList);
        cursor.close();
        db.close();
    }


    private void loadItems(){
        //db = dbHelper.getWritableDatabase();
        if(!((MainApp)getActivity()).checkNetworkConnection()) {
            readFromLocalDB();
        }else {
            foodListList = new ArrayList<FoodModel>();
            StringRequest stringRequest = new StringRequest(Request.Method.GET, ITEM_URL,
                new Response.Listener<String>(){
                    @Override
                    public void onResponse(String response){
                        try {
                            //db.execSQL(dbHelper.DROP_ITEM_TABLE);
                            //db.execSQL(dbHelper.CREATE_ITEM_TABLE);
<<<<<<< HEAD

=======
                            String url = "";
>>>>>>> 9e3b33763f3e6ef32080d1b0fffeea1543eb516c
                            JSONArray items = new JSONArray(response);
                            for(int i = 0; i < items.length(); i++){
                                JSONObject itemObject = items.getJSONObject(i);

                                String name = itemObject.getString("name");
                                String description = itemObject.getString("description");
                                String price = itemObject.getString("price");
                                String preptime = itemObject.getString("prep_time");
                                //int cat_id = itemObject.getInt("id");
<<<<<<< HEAD
                                String url = HOST + "images/" + itemObject.getString("url");
=======
                                if(!itemObject.getString("url").equals("")) url = HOST + itemObject.getString("url");
>>>>>>> 9e3b33763f3e6ef32080d1b0fffeea1543eb516c

                                //dbHelper.saveItemsToLocalDB(name, description, price, preptime, cat_id, db);
                                foodListList.add(new FoodModel(name, description, price, url, url));
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
<<<<<<< HEAD
            VolleySingelton.getInstance(getActivity().getApplicationContext()).addToRequestQueue(stringRequest);
=======
            Volley.newRequestQueue(getActivity().getApplicationContext()).add(stringRequest);
>>>>>>> 9e3b33763f3e6ef32080d1b0fffeea1543eb516c
        }
    }

    private void setRecentlyViewedRecycler(List<FoodModel> foodModelDataList) {
<<<<<<< HEAD
=======
//        GridLayoutManager gridLayoutManager = new GridLayoutManager(context,  new Utility().autoFitColumns(context, 150));
//        foodListRecycler.addItemDecoration(new GridSpacing(new Utility().calculateSpacing(context)));
//        foodListRecycler.setLayoutManager(gridLayoutManager);
>>>>>>> 9e3b33763f3e6ef32080d1b0fffeea1543eb516c
        foodListRecycler.setHasFixedSize(true);
        foodListRecycler.setItemAnimator(new DefaultItemAnimator());
        foodListAdapter = new FoodItemAdapter(context, fragmentManager, foodModelDataList);
        foodListRecycler.setAdapter(foodListAdapter);
    }
}
