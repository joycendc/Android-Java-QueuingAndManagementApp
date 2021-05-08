package com.giligans.queueapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.giligans.queueapp.R;
import com.giligans.queueapp.adapters.TabViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;


public class CategoriesFragment extends Fragment {

    final String CATEGORY_URL = "http://192.168.254.152/fetchcategory.php";
    private TabLayout tabLayout;
    private ViewPager firstViewPager;

    ArrayList<String> categories;
    private int[] tabIcons = {
            R.drawable.ic_fastfood_24dp
    };
    TabViewPagerAdapter adapter;

    public CategoriesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View rootView = inflater.inflate(R.layout.fragment_categories, container, false);

        firstViewPager = (ViewPager) rootView.findViewById(R.id.viewpager_content);

        tabLayout = (TabLayout) rootView.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(firstViewPager);
        adapter = new TabViewPagerAdapter(getChildFragmentManager());
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViewPager(firstViewPager);
    }

    private void setupViewPager(ViewPager viewPager) {

        StringRequest stringRequest = new StringRequest(Request.Method.GET, CATEGORY_URL,
            new Response.Listener<String>(){
                @Override
                public void onResponse(String response){
                    try {
                        JSONArray category = new JSONArray(response);

                        for(int i = 0; i < category.length(); i++){
                            JSONObject itemObject = category.getJSONObject(i);

                            int id = itemObject.getInt("id");
                            String name = itemObject.getString("name");
                            adapter.addFragment(new TabCategoryFragment(id), name);
                        }
                        firstViewPager.setAdapter(adapter);
                        for(int i = 0; i < category.length(); i++){
                             tabLayout.getTabAt(i).setIcon(tabIcons[0]);
                        }
                    } catch (JSONException e) { e.printStackTrace();  }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error){
                    Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        Volley.newRequestQueue(getContext()).add(stringRequest);

//        for(int i = 0; i < size; i++){
//            tabLayout.getTabAt(i).setIcon(tabIcons[0]);
//        }
    }
}