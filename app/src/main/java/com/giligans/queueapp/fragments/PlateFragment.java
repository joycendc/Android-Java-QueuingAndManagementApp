package com.giligans.queueapp.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.giligans.queueapp.MainActivity;
import com.giligans.queueapp.MainApp;
import com.giligans.queueapp.R;
import com.giligans.queueapp.models.CustomerModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.giligans.queueapp.adapters.PlateItemAdapter;
import com.giligans.queueapp.models.PlateModel;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Integer.parseInt;

public class PlateFragment extends Fragment {
    final String INSERT_URL = "http://192.168.254.152/insertorder.php";
    RecyclerView plateListRecycler;
    PlateItemAdapter plateItemAdapter;
    ArrayList<PlateModel> plateModel;
    Button placeOrder;
    TextView total;
    int totalAmount;

    public PlateFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plate, container, false);

        totalAmount = 0;
        plateModel = new ArrayList<PlateModel>();

        placeOrder = (Button) view.findViewById(R.id.placeOrder);
        plateListRecycler = (RecyclerView) view.findViewById(R.id.plateRecyclerView);
        placeOrder = (Button) view.findViewById(R.id.placeOrder);
        total = (TextView) view.findViewById(R.id.total);

        SharedPreferences sp = getActivity().getSharedPreferences("plate_list", Context.MODE_PRIVATE);
        String json = sp.getString("orderlist", null);
        if(json != null){
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<PlateModel>>() {}.getType();
            ArrayList<PlateModel> newList = new ArrayList<PlateModel>();
            newList = gson.fromJson(json, type);
            for(int i = 0; i < newList.size(); i++){
                totalAmount += (newList.get(i).getQty() * parseInt(newList.get(i).getPrice()));
                plateModel.add(new PlateModel(newList.get(i).getName(), newList.get(i).getPrice(), newList.get(i).getQty()));
            }
        }

        setPlateListRecycler(plateModel);
        if(totalAmount > 0){
            placeOrder.setEnabled(true);
            total.setText("TOTAL : Php " + Integer.toString(totalAmount));
        }

        placeOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sp = getActivity().getSharedPreferences("plate_list", Context.MODE_PRIVATE);
                sp.edit().clear().commit();
                plateModel = new ArrayList<PlateModel>();
                setPlateListRecycler(plateModel);
                totalAmount = 0;
                total.setText("");
                Toast.makeText(getActivity().getApplicationContext(), "Order Placed", Toast.LENGTH_SHORT).show();

                ((MainApp)getActivity()).setBadgeCount(0);
                ((MainApp)getActivity()).startTimer();

                insertToDB();

                FragmentTransaction ft =  getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, new LineFragment());
                ft.addToBackStack(null);
                ft.commit();
                // BottomNavigationView bottomNav = (BottomNavigationView) view.findViewById(R.id.navigation);
                ((MainApp)getActivity()).bottomNav.setSelectedItemId(R.id.navigation_line);

            }
        });
        // Inflate the layout for this fragment
        return view;
    }

    void insertToDB(){

        //setValues();

        // Creating string request with post method.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, INSERT_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String ServerResponse) {

                        // Hiding the progress dialog after all task complete.
                        //progressDialog.dismiss();

                        // Showing response message coming from server.
                        Toast.makeText(getContext(), ServerResponse, Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                        // Hiding the progress dialog after all task complete.
                        //progressDialog.dismiss();

                        // Showing error message if something goes wrong.
                        Toast.makeText(getContext(), volleyError.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {

                // Creating Map String Params.
                Map<String, String> params = new HashMap<String, String>();

                // Adding All values to Params.
                params.put("customer", "Jane Doe");
                params.put("orderlist", "Hakdoggzz");
                params.put("total", "1000");

                return params;
            }

        };

        // Creating RequestQueue.
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());

        // Adding the StringRequest object into requestQueue.
        requestQueue.add(stringRequest);
    }

    private void setPlateListRecycler(List<PlateModel> plateModel) {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1, GridLayoutManager.VERTICAL, false);
        plateListRecycler.setLayoutManager(gridLayoutManager);
        plateItemAdapter = new PlateItemAdapter(getContext(), plateModel);
        plateListRecycler.setAdapter(plateItemAdapter);
    }
}