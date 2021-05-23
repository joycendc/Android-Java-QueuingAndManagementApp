package com.giligans.queueapp.fragments;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.giligans.queueapp.DBContract;
import com.giligans.queueapp.DBHelper;
import com.giligans.queueapp.MainApp;
import com.giligans.queueapp.R;
import com.giligans.queueapp.adapters.TabViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.giligans.queueapp.BuildConfig.HOST;

public class CategoriesFragment extends Fragment {
    final String CATEGORY_URL = HOST + "fetchcategory.php";
    final String categoryImage = HOST + "images/drinks.png";
    private TabLayout tabLayout;
    private ViewPager firstViewPager;
    TabViewPagerAdapter adapter;
    ArrayList<String> categories;
    private int[] tabIcons = {
            R.drawable.ic_fastfood_24dp
    };
    Context context;
    DBHelper dbHelper;
    SQLiteDatabase db;

    public CategoriesFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getActivity();
        View rootView = inflater.inflate(R.layout.fragment_categories, container, false);
        dbHelper = new DBHelper(context);
        firstViewPager = (ViewPager) rootView.findViewById(R.id.viewpager_content);

        tabLayout = (TabLayout) rootView.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(firstViewPager);
        adapter = new TabViewPagerAdapter(getChildFragmentManager(), context);


        return rootView;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = new DBHelper(context);
        setupViewPager(firstViewPager);
    }



    void readFromLocalDB(){
        db = dbHelper.getReadableDatabase();
        Cursor cursor = dbHelper.readCatFromLocalDB(db);
        int count = 0;
        if(cursor != null){

            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex(DBContract.CATID));
                String name = cursor.getString(cursor.getColumnIndex(DBContract.CAT_NAME));
                adapter.addFragment(new TabCategoryFragment(id), name);
                count++;
            }
        }else{
            Toast.makeText(context, "PLEASE CONNECT TO OUR WIFI", Toast.LENGTH_SHORT).show();
        }
        firstViewPager.setAdapter(adapter);
        for (int i = 0; i < count; i++) {
            tabLayout.getTabAt(i).setIcon(tabIcons[0]);
        }
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
                            ImageView icon = (ImageView) tab.findViewById(R.id.icon);
//                            tabText.setText("All");
//                            tabLayout.getTabAt(0).setIcon(tabIcons[0]);
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
                                icon = (ImageView) tab.findViewById(R.id.icon);

                                Glide.with(getContext())
                                        .load(categoryImage)
                                        .placeholder(R.drawable.ic_fastfood_24dp)
                                        .into(icon);
                               // tabLayout.getTabAt(i).setIcon(tabIcons[0]);
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
                            adapter.SetOnSelectView(tabLayout, 0);

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
            Volley.newRequestQueue(context).add(stringRequest);
        }
    }
}