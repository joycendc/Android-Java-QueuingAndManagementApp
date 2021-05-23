package com.giligans.queueapp.fragments;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
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
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.giligans.queueapp.DBContract;
import com.giligans.queueapp.DBHelper;
import com.giligans.queueapp.MainApp;
import com.giligans.queueapp.R;
import com.giligans.queueapp.adapters.FoodItemAdapter;
import com.giligans.queueapp.models.FoodModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.giligans.queueapp.BuildConfig.HOST;

public class HomeFragment extends Fragment {
    String QUERY_URL = HOST + "fetchitems.php";
    final String ITEM_URL = HOST + "fetchitems.php";
    RecyclerView favoriteFoodsRecycler;
    FoodItemAdapter foodItemAdapter;
    List<FoodModel> foodModelList;
    EditText editText;
    FragmentManager fragmentManager;
    TextView alltime;
    Context context;
    DBHelper dbHelper;
    SQLiteDatabase db;

    public HomeFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getActivity();
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
                    alltime.setText("All Time Favorite");
                } else {
                    alltime.setText("Search for \"" + s.toString() + "\"");
                }
                QUERY_URL = HOST + "searchitems.php?query=" + s.toString();
                loadItems(QUERY_URL);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        dbHelper = new DBHelper(context);
        loadItems();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(isAdded()) {
            setRecentlyViewedRecycler(foodModelList);
            fragmentManager = getFragmentManager();
        }
    }

    void readFromLocalDB(){
        db = dbHelper.getReadableDatabase();
        foodModelList = new ArrayList<FoodModel>();
        Cursor cursor = dbHelper.readItemsFromLocalDB(db);

        if(cursor != null){
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex(DBContract.NAME));
                String description = cursor.getString(cursor.getColumnIndex(DBContract.DESC));
                String price = cursor.getString(cursor.getColumnIndex(DBContract.PRICE));
                foodModelList.add(new FoodModel(name, description, price, "", ""));
            }
        }else{
            Toast.makeText(context, "PLEASE CONNECT TO OUR WIFI", Toast.LENGTH_SHORT).show();
        }
        setRecentlyViewedRecycler(foodModelList);
        cursor.close();
        db.close();

    }

    void readFromLocalDB(String query){
        db = dbHelper.getReadableDatabase();
        foodModelList = new ArrayList<FoodModel>();
        Cursor cursor;
        if(query.equals("")) {
            cursor = dbHelper.readItemsFromLocalDB(db);
        } else {
            cursor = dbHelper.readItemsFromLocalDB(db, query);
        }
        if(cursor != null){
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex(DBContract.NAME));
                String description = cursor.getString(cursor.getColumnIndex(DBContract.DESC));
                String price = cursor.getString(cursor.getColumnIndex(DBContract.PRICE));
                foodModelList.add(new FoodModel(name, description, price, "", ""));
            }
        }else{
            Toast.makeText(context, "PLEASE CONNECT TO OUR WIFI", Toast.LENGTH_SHORT).show();
        }
        foodItemAdapter.updateList(foodModelList);
        cursor.close();
        db.close();

    }

    private void loadItems(){
        db = dbHelper.getWritableDatabase();
        if(!((MainApp)getActivity()).checkNetworkConnection()) {
            readFromLocalDB();
        }else {
            foodModelList = new ArrayList<FoodModel>();
            StringRequest stringRequest = new StringRequest(Request.Method.GET, ITEM_URL,
                new Response.Listener<String>(){
                    @Override
                    public void onResponse(String response){
                        try {
                            db.execSQL(dbHelper.DROP_ITEM_TABLE);
                            db.execSQL(dbHelper.CREATE_ITEM_TABLE);
                            JSONArray items = new JSONArray(response);
                            String url = "";
                            for(int i = 0; i < items.length(); i++){
                                JSONObject itemObject = items.getJSONObject(i);

                                String name = itemObject.getString("name");
                                String description = itemObject.getString("description");
                                String price = itemObject.getString("price");
                                String preptime = itemObject.getString("prep_time");
                                int cat_id = itemObject.getInt("cat_id");
                                if(!itemObject.getString("url").equals("")) url = HOST + itemObject.getString("url");
                                dbHelper.saveItemsToLocalDB(name, description, price, preptime, cat_id, db);
                                foodModelList.add(new FoodModel(name, description, price, url, url));
                            }
                            setRecentlyViewedRecycler(foodModelList);
                        } catch (JSONException e) {
                            Toast.makeText(context, "READ not", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error){
                        readFromLocalDB();
                    }
                });
            Volley.newRequestQueue(getActivity().getApplicationContext()).add(stringRequest);
        }
}

    private void loadItems(String query){
        if(!((MainApp)getActivity()).checkNetworkConnection()) {
            Uri uri = Uri.parse(query);
            String param = uri.getQueryParameter("query");
            readFromLocalDB(param);
        }else {
            foodModelList = new ArrayList<>();
            StringRequest stringRequest = new StringRequest(Request.Method.GET, QUERY_URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONArray items = new JSONArray(response);
                                for (int i = 0; i < items.length(); i++) {
                                    JSONObject itemObject = items.getJSONObject(i);

                                    String name = itemObject.getString("name");
                                    String description = itemObject.getString("description");
                                    String price = itemObject.getString("price");
                                    foodModelList.add(new FoodModel(name, description, price, "", ""));
                                }
                                foodItemAdapter.updateList(foodModelList);
                            } catch (JSONException e) {
                                Toast.makeText(context, "READ not", Toast.LENGTH_SHORT).show();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Uri uri = Uri.parse(query);
                            String param = uri.getQueryParameter("query");
                            readFromLocalDB(param);
                        }
                    });
            Volley.newRequestQueue(getActivity().getApplicationContext()).add(stringRequest);
        }
    }

    private void setRecentlyViewedRecycler(List<FoodModel> foodModelDataList) {
        //GridLayoutManager gridLayoutManager = new GridLayoutManager(context, new Utility().autoFitColumns(context, 150));
        //favoriteFoodsRecycler.addItemDecoration(new GridSpacing(new Utility().calculateSpacing(context)));
        //favoriteFoodsRecycler.setLayoutManager(gridLayoutManager);
        favoriteFoodsRecycler.setHasFixedSize(true);
        favoriteFoodsRecycler.setItemAnimator(new DefaultItemAnimator());
        foodItemAdapter = new FoodItemAdapter(context, fragmentManager, foodModelDataList);
        favoriteFoodsRecycler.setAdapter(foodItemAdapter);
    }
}