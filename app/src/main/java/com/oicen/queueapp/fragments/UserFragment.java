package com.oicen.queueapp.fragments;

import static com.oicen.queueapp.BuildConfig.HOST;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.oicen.queueapp.R;
import com.oicen.queueapp.activities.MainApp;
import com.oicen.queueapp.utils.ApiHelper;
import com.oicen.queueapp.utils.VolleySingleton;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UserFragment extends Fragment {
    String UPDATE_USER = HOST + ApiHelper.UPDATE_USER;
    EditText fname, lname, mobile;
    ImageView back;
    Button update, cancel;

    public UserFragment(){ }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        fname = (EditText) view.findViewById(R.id.fname);
        lname = (EditText) view.findViewById(R.id.lname);
        back = (ImageView) view.findViewById(R.id.back2);
        mobile = (EditText) view.findViewById(R.id.confirmNumber);
        update = (Button) view.findViewById(R.id.button);
        cancel = (Button) view.findViewById(R.id.cancel);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainApp)getActivity()).onBackPressed();
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(update.getText().equals("Edit")){
                    toggleInput(fname, true);
                    toggleInput(lname, true);
                    toggleInput(mobile, true);
                }else{
                    toggleInput(fname, false);
                    toggleInput(lname, false);
                    toggleInput(mobile, false);

                    updateUser();

                }

                update.setText(update.getText().equals("Edit") ? "Update" : "Edit");
                update.setBackgroundColor(update.getText().equals("Edit") ? getResources().getColor(R.color.colorPrimary) : Color.parseColor("#009d00"));
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                update.setText("Edit");
                update.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                toggleInput(fname, false);
                toggleInput(lname, false);
                toggleInput(mobile, false);
                cancel.setVisibility(View.INVISIBLE);
            }
        });

        return view;
    }


    void toggleInput(EditText input, Boolean bool){
        input.setFocusable(bool);
        input.setFocusableInTouchMode(bool);
        if(bool){
            input.setBackgroundColor(Color.parseColor("#eeeeee"));
            cancel.setVisibility(View.VISIBLE);
        }else {
            input.setBackgroundColor(Color.parseColor("#ffffff"));
            cancel.setVisibility(View.INVISIBLE);
        }
    }

    void updateUser(){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, UPDATE_USER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            String status = obj.getString("status");
                            if (!obj.getBoolean("error")) {

                                Toast.makeText(getActivity().getApplicationContext(), status, Toast.LENGTH_SHORT).show();
                            }
                            Toast.makeText(getActivity().getApplicationContext(), status, Toast.LENGTH_SHORT).show();

                        }catch (Exception e){
                            Toast.makeText(getActivity().getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();

                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                SharedPreferences sharedPreferences = getContext().getSharedPreferences("login", Context.MODE_PRIVATE);
                String id = sharedPreferences.getString("keyid", null);
                params.put("customer_id", id);
                params.put("first_name", fname.getText().toString());
                params.put("last_name", lname.getText().toString());
                params.put("mobile_number", mobile.getText().toString());

                return params;
            }
        };
        VolleySingleton.getInstance(getContext()).addToRequestQueue(stringRequest);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("login", Context.MODE_PRIVATE);
        fname.setText(sharedPreferences.getString("keyfname", null));
        lname.setText(sharedPreferences.getString("keylname", null));
        mobile.setText(sharedPreferences.getString("keymobile", null));
    }
}