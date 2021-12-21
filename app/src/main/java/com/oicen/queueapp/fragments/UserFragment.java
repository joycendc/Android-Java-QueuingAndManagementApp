package com.oicen.queueapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
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

import com.oicen.queueapp.R;
import com.oicen.queueapp.activities.MainApp;

public class UserFragment extends Fragment {
    EditText fname, lname, mobile;
    ImageView back;
    Button update;

    public UserFragment(){ }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        fname = (EditText) view.findViewById(R.id.fname);
        lname = (EditText) view.findViewById(R.id.lname);
        back = (ImageView) view.findViewById(R.id.back2);
        mobile = (EditText) view.findViewById(R.id.confirmNumber);
        update = (Button) view.findViewById(R.id.button);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainApp)getActivity()).onBackPressed();
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(update.getText().equals("EDIT")){
                    toggleInput(fname, true);
                    toggleInput(lname, true);
                    toggleInput(mobile, true);
                }else{
                    toggleInput(fname, false);
                    toggleInput(lname, false);
                    toggleInput(mobile, false);
                    Toast.makeText(getContext(), "Updated", Toast.LENGTH_SHORT).show();
                }

                update.setText(update.getText().equals("EDIT") ? "UPDATE" : "EDIT");
            }
        });

        return view;
    }

    void toggleInput(EditText input, Boolean bool){
//        input.setFocusable(bool);
//        input.setClickable(bool);
//        input.setCursorVisible(bool);

    }

    void updateUser(){
//        StringRequest stringRequest = new StringRequest(Request.Method.POST, FAVE,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        try {
//                            JSONObject obj = new JSONObject(response);
//                            if (!obj.getBoolean("error")) {
//                                String status = obj.getString("status");
//
//                                Toast.makeText(getActivity().getApplicationContext(), status, Toast.LENGTH_SHORT).show();
//                            }
//                        }catch (Exception e){
//                            isFave = false;
//                            e.printStackTrace();
//                        }
//                    }
//                },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//
//                    }
//                }) {
//            @Override
//            protected Map<String, String> getParams() throws AuthFailureError {
//                Map<String, String> params = new HashMap<String, String>();
//                SharedPreferences sharedPreferences = getContext().getSharedPreferences("login", Context.MODE_PRIVATE);
//                String id = sharedPreferences.getString("keyid", null);
//                params.put("customer_id", id);
//                params.put("item_id", String.valueOf(item_id));
//                params.put("fave", mode);
//                return params;
//            }
//        };
//        VolleySingleton.getInstance(getContext()).addToRequestQueue(stringRequest);
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