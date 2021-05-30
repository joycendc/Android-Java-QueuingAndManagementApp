package com.giligans.queueapp.fragments;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.giligans.queueapp.AutoFitRecyclerView;
import com.giligans.queueapp.DBContract;
import com.giligans.queueapp.DBHelper;
import com.giligans.queueapp.MainApp;
import com.giligans.queueapp.R;
import com.giligans.queueapp.VolleySingelton;
import com.giligans.queueapp.adapters.FoodItemAdapter;
import com.giligans.queueapp.adapters.TabViewPagerAdapter;
import com.giligans.queueapp.models.FoodModel;
import com.google.android.material.tabs.TabLayout;
import com.qhutch.elevationimageview.ElevationImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.giligans.queueapp.BuildConfig.HOST;

public class HomeFragment extends Fragment {
    String QUERY_URL = HOST + "fetchitems.php";
    final String CATEGORY_URL = HOST + "fetchcategory.php";
    public TabLayout tabLayout;
    public ViewPager firstViewPager;
    public TabViewPagerAdapter adapter;
    ArrayList<String> categories;
    public final String[] tabIcons = {
            HOST + "images/sweetandsourpork.png",
            HOST + "images/friedgarlicchicken.png",
            HOST + "images/beefcaldereta.png",
            HOST + "images/sweetandspicysquid.png",
            HOST + "images/nilagangbaka.png",
            HOST + "images/drinks.png",
            HOST + "images/lecheflan.png",
            HOST + "images/plainriceplatter.png",
            HOST + "images/pancitcantonguisado.png",
            HOST + "images/kare-karenggulay.png",
    };

    FragmentManager fragmentManager;
    RecyclerView favoriteFoodsRecycler;
    FoodItemAdapter foodItemAdapter;
    List<FoodModel> foodModelList;
    Context context;
    DBHelper dbHelper;
    SQLiteDatabase db;
    public EditText editText;
    AutoFitRecyclerView recently_item;
    LinearLayout categoryview;
    int pos;

    public HomeFragment() {
        this.pos = 0;
    }

    public HomeFragment(int pos) {
        this.pos = pos;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getActivity();
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        dbHelper = new DBHelper(context);
        favoriteFoodsRecycler = (RecyclerView) rootView.findViewById(R.id.recently_item);
        firstViewPager = (ViewPager) rootView.findViewById(R.id.viewpager_content);
        editText = (EditText) rootView.findViewById(R.id.editText);
        recently_item = (AutoFitRecyclerView) rootView.findViewById(R.id.recently_item);
        categoryview = (LinearLayout) rootView.findViewById(R.id.categoryview);

        tabLayout = (TabLayout) rootView.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(firstViewPager);
        adapter = new TabViewPagerAdapter(getChildFragmentManager(), context);


        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() == 0) {
                    recently_item.setVisibility(View.GONE);
                    categoryview.setVisibility(View.VISIBLE);
                } else {
                    QUERY_URL = HOST + "searchitems.php?query=" + s.toString();
                    recently_item.setVisibility(View.VISIBLE);
                    categoryview.setVisibility(View.GONE);
                    loadItems(QUERY_URL);
                }

            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        return rootView;
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
                                    String preptime = itemObject.getString("prep_time");
                                    int cat_id = itemObject.getInt("cat_id");
                                    String url = HOST + "images/" + itemObject.getString("url");

                                    foodModelList.add(new FoodModel(name, description, price, url, url));
                                }
                                foodItemAdapter.updateList(foodModelList);
                            } catch (JSONException e) {
                                Toast.makeText(context, e.getMessage() + " load query", Toast.LENGTH_SHORT).show();
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
            VolleySingelton.getInstance(getActivity().getApplicationContext()).addToRequestQueue(stringRequest);
            //Volley.newRequestQueue(getActivity().getApplicationContext()).add(stringRequest);
        }
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = new DBHelper(context);
        setupViewPager(firstViewPager);
        if(isAdded()) {
            setRecentlyViewedRecycler(foodModelList);
            fragmentManager = getFragmentManager();
        }
    }



    void readFromLocalDB(){
        db = dbHelper.getReadableDatabase();
        Cursor cursor = dbHelper.readCatFromLocalDB(db);

        if(cursor != null){

            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex(DBContract.CATID));
                String name = cursor.getString(cursor.getColumnIndex(DBContract.CAT_NAME));
                adapter.addFragment(new TabCategoryFragment(id), name);
            }
        }else{
            Toast.makeText(context, "PLEASE CONNECT TO OUR WIFI", Toast.LENGTH_SHORT).show();
        }

        firstViewPager.setAdapter(adapter);

        CardView tab = (CardView) LayoutInflater.from(context.getApplicationContext()).inflate(R.layout.custom_tab, null);
        TextView tabText = (TextView) tab.findViewById(R.id.title);
        ElevationImageView icon = (ElevationImageView) tab.findViewById(R.id.icon);

        cursor = dbHelper.readCatFromLocalDB(db);
        if(cursor != null) {
            int i = 0;
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex(DBContract.CAT_NAME));

                tab = (CardView) LayoutInflater.from(context.getApplicationContext()).inflate(R.layout.custom_tab, null);
                tab.setCardBackgroundColor(Color.TRANSPARENT);
                tab.setCardElevation(0);
                tabText = (TextView) tab.findViewById(R.id.title);
                TypedValue typedValue = new TypedValue();
                ((Activity) context).getTheme().resolveAttribute(R.attr.categoryText, typedValue, true);
                tabText.setTextColor(typedValue.data);
                tabText.setText(name);
                icon = (ElevationImageView) tab.findViewById(R.id.icon);

                icon.setImageResource(R.drawable.ic_fastfood_24dp);
                icon.setColorFilter(R.color.black, android.graphics.PorterDuff.Mode.MULTIPLY);

                tabLayout.getTabAt(i).setCustomView(tab);
                i++;
            }
        }

        int betweenSpace = 25;

        ViewGroup slidingTabStrip = (ViewGroup) tabLayout.getChildAt(0);

        for (int i=0; i < slidingTabStrip.getChildCount(); i++) {
            View v = slidingTabStrip.getChildAt(i);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            params.rightMargin = betweenSpace;
        }

        tabLayout.setOnTabSelectedListener(
            new TabLayout.OnTabSelectedListener(){
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    int pos = tab.getPosition();
                    ((MainApp) Objects.requireNonNull(getActivity())).tabPos = pos;
                    adapter.SetOnSelectView(tabLayout, pos);
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                    int pos = tab.getPosition();
                    adapter.SetUnSelectView(tabLayout, pos);
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                    int pos = tab.getPosition();
                    adapter.SetOnSelectView(tabLayout, pos);
                }
            }
        );
        adapter.SetOnSelectView(tabLayout, 1);

        cursor.close();
        db.close();
    }

    private void setupViewPager(ViewPager viewPager) {
        db = dbHelper.getWritableDatabase();
        if(!((MainApp)getActivity()).checkNetworkConnection()) {
            readFromLocalDB();
        }else {
            StringRequest stringRequest = new StringRequest(Request.Method.GET, CATEGORY_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                    try {
                        db.execSQL(dbHelper.DROP_CAT_TABLE);
                        db.execSQL(dbHelper.CREATE_CAT_TABLE);
                        JSONArray category = new JSONArray(response);

//                            adapter.addFragment(new TabCategoryFragment(0), "All");

                        for (int i = 0; i < category.length(); i++) {
                            JSONObject itemObject = category.getJSONObject(i);

                            int id = itemObject.getInt("id");
                            String name = itemObject.getString("name");
                            adapter.addFragment(new TabCategoryFragment(id), name);
                            dbHelper.saveCatToLocalDB(name, db);
                        }
                        firstViewPager.setAdapter(adapter);


                        CardView tab = (CardView) LayoutInflater.from(context.getApplicationContext()).inflate(R.layout.custom_tab, null);
//                            tab.setCardBackgroundColor(Color.TRANSPARENT);
//                            tab.setCardElevation(0);
                        TextView tabText = (TextView) tab.findViewById(R.id.title);
                        ElevationImageView icon = (ElevationImageView) tab.findViewById(R.id.icon);
//                            tabText.setText("All");
//                            tabLayout.getTabAt(0).setCustomView(tab);

                        for (int i = 0; i < category.length(); i++) {
                            JSONObject itemObject = category.getJSONObject(i);
                            String name = itemObject.getString("name");

                            tab = (CardView) LayoutInflater.from(context.getApplicationContext()).inflate(R.layout.custom_tab, null);
                            tab.setCardBackgroundColor(Color.TRANSPARENT);
                            tab.setCardElevation(0);
                            tabText = (TextView) tab.findViewById(R.id.title);
                            TypedValue typedValue = new TypedValue();
                            ((Activity) context).getTheme().resolveAttribute(R.attr.categoryText, typedValue, true);
                            tabText.setTextColor(typedValue.data);
                            tabText.setText(name);
                            icon = (ElevationImageView) tab.findViewById(R.id.icon);

                            Glide.with(context)
                                .load(tabIcons[i])
                                .placeholder(R.drawable.ic_fastfood_24dp)
                                .dontAnimate()
                                .into(icon);
                            icon.setElevation(8);

                            tabLayout.getTabAt(i).setCustomView(tab);
                        }

                        int betweenSpace = 25;

                        ViewGroup slidingTabStrip = (ViewGroup) tabLayout.getChildAt(0);

                        for (int i=0; i < slidingTabStrip.getChildCount(); i++) {
                            View v = slidingTabStrip.getChildAt(i);
                            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                            params.rightMargin = betweenSpace;
                        }

                        tabLayout.setOnTabSelectedListener(
                            new TabLayout.OnTabSelectedListener(){
                                @Override
                                public void onTabSelected(TabLayout.Tab tab) {
                                    int pos = tab.getPosition();
                                    ((MainApp) Objects.requireNonNull(getActivity())).tabPos = pos;
                                    adapter.SetOnSelectView(tabLayout, pos);
                                }

                                @Override
                                public void onTabUnselected(TabLayout.Tab tab) {
                                    int pos = tab.getPosition();
                                    adapter.SetUnSelectView(tabLayout, pos);
                                }

                                @Override
                                public void onTabReselected(TabLayout.Tab tab) {
                                    int pos = tab.getPosition();
                                    adapter.SetOnSelectView(tabLayout, pos);
                                }
                            }
                        );


                        tabLayout.setScrollPosition(pos,0f,true);
                        viewPager.setCurrentItem(pos);
                        adapter.SetOnSelectView(tabLayout, pos);

                    } catch (JSONException e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        readFromLocalDB();
                    }
                });
            VolleySingelton.getInstance(context).addToRequestQueue(stringRequest);
            //Volley.newRequestQueue(context).add(stringRequest);
        }
    }

    private void setRecentlyViewedRecycler(List<FoodModel> foodModelDataList) {
        favoriteFoodsRecycler.setHasFixedSize(true);
        favoriteFoodsRecycler.setItemAnimator(new DefaultItemAnimator());
        foodItemAdapter = new FoodItemAdapter(context, fragmentManager, foodModelDataList);
        foodItemAdapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
        favoriteFoodsRecycler.setAdapter(foodItemAdapter);
    }
}