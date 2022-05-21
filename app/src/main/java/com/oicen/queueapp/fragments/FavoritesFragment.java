package com.oicen.queueapp.fragments;

import static com.oicen.queueapp.BuildConfig.HOST;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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
import com.oicen.queueapp.adapters.FoodItemAdapter;
import com.oicen.queueapp.models.FoodModel;
import com.oicen.queueapp.utils.ApiHelper;
import com.oicen.queueapp.utils.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FavoritesFragment extends Fragment {
    final String FAVE = HOST + ApiHelper.FAVE;
    RecyclerView favoriteFoodsRecycler;
    FoodItemAdapter foodItemAdapter;
    ArrayList<FoodModel> foodModelList;
    FragmentManager fragmentManager;
    TextView alltime, empty;
    Context context;
    ShimmerFrameLayout shimmerFrameLayout;
    private View view = null;

    public FavoritesFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getActivity();
        if(view != null) return view;
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);
        favoriteFoodsRecycler = (RecyclerView) view.findViewById(R.id.recently_item);
        alltime = (TextView) view.findViewById(R.id.alltime);
        empty = (TextView) view.findViewById(R.id.empty);
        shimmerFrameLayout = (ShimmerFrameLayout) view.findViewById(R.id.shimmerLayout);

        foodModelList = new ArrayList<>();
        shimmerFrameLayout.startShimmer();
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        loadItems();

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
        foodModelList = new ArrayList<FoodModel>();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, FAVE,
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
                            String url = HOST + ApiHelper.IMAGE_PATH  + itemObject.getString("url");

                            foodModelList.add(new FoodModel(id, name, description, price, url, url));
                        }
                        shimmerFrameLayout.stopShimmer();
                        shimmerFrameLayout.setVisibility(View.GONE);
                        setRecentlyViewedRecycler(foodModelList);
                        if(foodItemAdapter.getItemCount() == 0){
                            empty.setVisibility(View.VISIBLE);
                        }else{
                            empty.setVisibility(View.INVISIBLE);
                        }
                    } catch (JSONException e) {
                        Toast.makeText(context, e.getMessage() + " load", Toast.LENGTH_SHORT).show();
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error){
                    Toast.makeText(context, error.getMessage() + " load", Toast.LENGTH_SHORT).show();

                }
            }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                SharedPreferences sharedPreferences = context.getSharedPreferences("login", Context.MODE_PRIVATE);
                String id = sharedPreferences.getString("keyid", "");
                params.put("customer_id", id);
                params.put("item_id", "");
                params.put("fave", "fetch");
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

    private void setRecentlyViewedRecycler(List<FoodModel> foodModelDataList) {
        favoriteFoodsRecycler.setHasFixedSize(true);
        favoriteFoodsRecycler.setItemAnimator(new DefaultItemAnimator());
        foodItemAdapter = new FoodItemAdapter(context, fragmentManager, foodModelDataList);
        foodItemAdapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
        favoriteFoodsRecycler.setAdapter(foodItemAdapter);
    }
}