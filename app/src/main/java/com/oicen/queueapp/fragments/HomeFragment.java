package com.oicen.queueapp.fragments;

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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
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

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.oicen.queueapp.R;
import com.oicen.queueapp.activities.MainApp;
import com.oicen.queueapp.adapters.FoodItemAdapter;
import com.oicen.queueapp.adapters.TabViewPagerAdapter;
import com.oicen.queueapp.models.FoodModel;
import com.oicen.queueapp.utils.ApiHelper;
import com.oicen.queueapp.utils.AutoFitRecyclerView;
import com.oicen.queueapp.utils.DBContract;
import com.oicen.queueapp.utils.DBHelper;
import com.oicen.queueapp.utils.GlideApp;
import com.oicen.queueapp.utils.VolleySingleton;
import com.google.android.material.tabs.TabLayout;
import com.qhutch.elevationimageview.ElevationImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.oicen.queueapp.BuildConfig.HOST;

public class HomeFragment extends Fragment {
    String QUERY_URL = HOST + ApiHelper.QUERY_URL;
    final String CATEGORY_URL = HOST + ApiHelper.CATEGORY_URL;
    public TabLayout tabLayout;
    public ViewPager firstViewPager;
    public TabViewPagerAdapter adapter;
    ArrayList<String> categories;
    public String[] tabIcons = null;

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
    ShimmerFrameLayout shimmerFrameLayout;
    private View rootView = null;

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
        if(rootView != null) return rootView;
        rootView = inflater.inflate(R.layout.fragment_home, container, false);
        dbHelper = new DBHelper(context);
        favoriteFoodsRecycler = (RecyclerView) rootView.findViewById(R.id.recently_item);
        firstViewPager = (ViewPager) rootView.findViewById(R.id.viewpager_content);
        editText = (EditText) rootView.findViewById(R.id.editText);
        recently_item = (AutoFitRecyclerView) rootView.findViewById(R.id.recently_item);
        categoryview = (LinearLayout) rootView.findViewById(R.id.categoryview);
        shimmerFrameLayout = (ShimmerFrameLayout) rootView.findViewById(R.id.shimmerLayout);

        tabLayout = (TabLayout) rootView.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(firstViewPager);
        adapter = new TabViewPagerAdapter(getChildFragmentManager(), context);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() == 0) {
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    recently_item.setVisibility(View.GONE);
                    categoryview.setVisibility(View.VISIBLE);
                } else {
                    QUERY_URL = HOST + "searchItems.php?query=" + s.toString();
                    shimmerFrameLayout.startShimmer();
                    shimmerFrameLayout.setVisibility(View.VISIBLE);
                    categoryview.setVisibility(View.GONE);
                    loadItems(QUERY_URL);
                    System.out.println(QUERY_URL);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    handled = true;
                }
                return handled;
            }
        });
        shimmerFrameLayout.startShimmer();
        shimmerFrameLayout.setVisibility(View.VISIBLE);

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
                int id = cursor.getInt(cursor.getColumnIndex(DBContract.ID));
                String name = cursor.getString(cursor.getColumnIndex(DBContract.NAME));
                String description = cursor.getString(cursor.getColumnIndex(DBContract.DESC));
                String price = cursor.getString(cursor.getColumnIndex(DBContract.PRICE));
                foodModelList.add(new FoodModel(id, name, description, price, "", ""));
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

                                int id = itemObject.getInt("id");
                                String name = itemObject.getString("name");
                                String description = itemObject.getString("description");
                                String price = itemObject.getString("price");
                                int cat_id = itemObject.getInt("cat_id");
                                String url = HOST + ApiHelper.IMAGE_PATH + itemObject.getString("url");

                                foodModelList.add(new FoodModel(id, name, description, price, url, url));
                            }
                            foodItemAdapter.updateList(foodModelList);
                            shimmerFrameLayout.stopShimmer();
                            shimmerFrameLayout.setVisibility(View.GONE);
                            recently_item.setVisibility(View.VISIBLE);
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
                }) {
                // @Override
                // public Map<String, String> getHeaders() throws AuthFailureError {
                //     Map<String, String> headers = new HashMap<>();
                //     headers.put(ApiHelper.KEY_COOKIE, ApiHelper.VALUE_CONTENT);
                //     return headers;
                // }
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0, -1,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            VolleySingleton.getInstance(getActivity().getApplicationContext()).addToRequestQueue(stringRequest);
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

    @Override
    public void onViewStateRestored(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
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
                    ((MainApp) requireActivity()).tabPos = pos;
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
                        tabIcons = new String[category.length()];

                        for (int i = 0; i < category.length(); i++) {
                            JSONObject itemObject = category.getJSONObject(i);

                            int id = itemObject.getInt("id");
                            String name = itemObject.getString("name");

                            tabIcons[i] = HOST + ApiHelper.CAT_PATH + name + ".png";

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

                            GlideApp.with(context.getApplicationContext())
                                .load(tabIcons[i])
                                .placeholder(R.drawable.ic_fastfood_24dp)
                                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE)
                                .error(R.drawable.ic_fastfood_24dp))
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
                                    ((MainApp) requireActivity()).tabPos = pos;
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
                        shimmerFrameLayout.stopShimmer();
                        shimmerFrameLayout.setVisibility(View.GONE);

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
                }) {
                // @Override
                // public Map<String, String> getHeaders() throws AuthFailureError {
                //     Map<String, String> headers = new HashMap<>();
                //     headers.put(ApiHelper.KEY_COOKIE, ApiHelper.VALUE_CONTENT);
                //     return headers;
                // }
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0, -1,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(context).addToRequestQueue(stringRequest);
        }
    }

    private void setRecentlyViewedRecycler(List<FoodModel> foodModelDataList) {
        favoriteFoodsRecycler.setHasFixedSize(true);
        favoriteFoodsRecycler.setItemAnimator(new DefaultItemAnimator());
        foodItemAdapter = new FoodItemAdapter(context, fragmentManager, foodModelDataList);
        favoriteFoodsRecycler.setAdapter(foodItemAdapter);
    }
}