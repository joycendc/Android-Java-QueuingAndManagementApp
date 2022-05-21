package com.oicen.queueapp.fragments;

import static com.oicen.queueapp.BuildConfig.HOST;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.oicen.queueapp.R;
import com.oicen.queueapp.adapters.FoodItemAdapter;
import com.oicen.queueapp.adapters.QueueAdapter;
import com.oicen.queueapp.adapters.RecentAdapter;
import com.oicen.queueapp.models.FoodModel;
import com.oicen.queueapp.models.QueueModel;
import com.oicen.queueapp.models.RecentModel;
import com.oicen.queueapp.models.RecentOrderItem;
import com.oicen.queueapp.utils.ApiHelper;
import com.oicen.queueapp.utils.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecentOrdersFragment extends Fragment {
    final String RECENT_ORDERS = HOST + ApiHelper.RECENT_ORDERS;
    RecyclerView recentRecycler;
    RecentAdapter recentAdapter;
    ArrayList<RecentModel> recents;
    FragmentManager fragmentManager;
    TextView empty;
    Context context;
    ShimmerFrameLayout shimmerFrameLayout;


    public RecentOrdersFragment(){ }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recent_orders, container, false);

        context = getActivity();

        recentRecycler = (RecyclerView) view.findViewById(R.id.recentRecyclerView);

        empty = (TextView) view.findViewById(R.id.empty);
        recents = new ArrayList<RecentModel>();

        loadItems();

        //shimmerFrameLayout = (ShimmerFrameLayout) view.findViewById(R.id.shimmerLayout);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(isAdded()) {
            fragmentManager = getFragmentManager();
        }
    }



    private void loadItems(){
        recents = new ArrayList<RecentModel>();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, RECENT_ORDERS,
                new Response.Listener<String>(){
                    @Override
                    public void onResponse(String response){
                        try {
                            JSONArray items = new JSONArray(response);

                            ArrayList<ArrayList<RecentOrderItem>> outer = new ArrayList<ArrayList<RecentOrderItem>>();
                            ArrayList<RecentOrderItem> inner = new ArrayList<RecentOrderItem>();
                            ArrayList<String> core = new ArrayList<String>();
                            ArrayList<String> done = new ArrayList<String>();

                            for(int i = 0; i < items.length(); i++){
                                JSONObject itemObject = items.getJSONObject(i);

                                String id = itemObject.getString("id");
                                String date = itemObject.getString("date");
                                String item_name = itemObject.getString("item_name");
                                String qty = itemObject.getString("qty");
                                String price = itemObject.getString("price");
                                String total = itemObject.getString("total");

                                if (done.contains(date)) continue;

                                for(int j = i + 1; j < items.length(); j++) {
                                    if(i == j - 1){
                                        inner.add(new RecentOrderItem(id, date, item_name, qty, price, total));
                                    }

                                    JSONObject itemObject2 = items.getJSONObject(j);
                                    String id2 = itemObject2.getString("id");
                                    String date2 = itemObject2.getString("date");
                                    String item_name2 = itemObject2.getString("item_name");
                                    String qty2 = itemObject2.getString("qty");
                                    String price2 = itemObject2.getString("price");
                                    String total2 = itemObject2.getString("total");

                                    if (date.equals(date2)){
                                        inner.add(new RecentOrderItem(id2, date2, item_name2, qty2, price2, total2));
                                    }
                                }

                                if(!done.contains(date)){
                                    if(i == items.length() - 1){
                                        inner.add(new RecentOrderItem(id, date, item_name, qty, price, total));
                                    }
                                    done.add(date);
                                }

                                if(!inner.isEmpty()) {
                                    outer.add(inner);
                                    inner = new ArrayList<RecentOrderItem>();
                                }
                            }

                            for(ArrayList<RecentOrderItem> innerList : outer){
                                recents.add(new RecentModel(innerList));

                                for(RecentOrderItem item : innerList){
                                    System.out.println(item.getItem_name() +
                                        " " + item.getDate() +
                                        " " + item.getPrice() + " " + item.getTotal());
                                }
                            }

                            setRecentOrdersRecycler(recents);

                            if(recentAdapter.getItemCount() == 0){
                                empty.setVisibility(View.VISIBLE);
                            }else{
                                empty.setVisibility(View.INVISIBLE);
                            }
                        } catch (JSONException e) {
                            Toast.makeText(context, e.getMessage() + " loadR", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error){
                        Toast.makeText(context, error.getMessage() + " loadR2", Toast.LENGTH_SHORT).show();

                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                SharedPreferences sharedPreferences = context.getSharedPreferences("login", Context.MODE_PRIVATE);
                String id = sharedPreferences.getString("keyid", "");
                params.put("customer_id", id);

                return params;
            }
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

    private void setRecentOrdersRecycler(ArrayList<RecentModel> recents) {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1, GridLayoutManager.VERTICAL, false);
        recentRecycler.setLayoutManager(gridLayoutManager);
        recentRecycler.setHasFixedSize(true);
        recentRecycler.setItemAnimator(new DefaultItemAnimator());
        recentAdapter = new RecentAdapter(context, fragmentManager, recents);
        recentAdapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
        recentRecycler.setAdapter(recentAdapter);
    }
}
