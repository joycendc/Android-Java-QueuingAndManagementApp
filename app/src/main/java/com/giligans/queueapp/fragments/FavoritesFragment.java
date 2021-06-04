package com.giligans.queueapp.fragments;

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
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.giligans.queueapp.activities.MainApp;
import com.giligans.queueapp.R;
import com.giligans.queueapp.utils.VolleySingleton;
import com.giligans.queueapp.adapters.FoodItemAdapter;
import com.giligans.queueapp.models.FoodModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.giligans.queueapp.BuildConfig.HOST;

public class FavoritesFragment extends Fragment {
    final String FAVE = HOST + "favorite.php";
    RecyclerView favoriteFoodsRecycler;
    FoodItemAdapter foodItemAdapter;
    ArrayList<FoodModel> foodModelList;
    FragmentManager fragmentManager;
    TextView alltime, empty;
    Context context;

    public FavoritesFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getActivity();
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);
        favoriteFoodsRecycler = (RecyclerView) view.findViewById(R.id.recently_item);
        alltime = (TextView) view.findViewById(R.id.alltime);
        empty = (TextView) view.findViewById(R.id.empty);
        foodModelList = new ArrayList<>();
        loadItems();
        if(((MainApp)getActivity()).guest){
            empty.setText("Not Available for Guest");
        }

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
//                                String preptime = itemObject.getString("prep_time");
//                                int cat_id = itemObject.getInt("cat_id");
                            String url = HOST + "images/" + itemObject.getString("url");

                            foodModelList.add(new FoodModel(id, name, description, price, url, url));
                        }
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
        };
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