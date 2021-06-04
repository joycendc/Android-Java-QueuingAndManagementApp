package com.giligans.queueapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.giligans.queueapp.activities.MainApp;
import com.giligans.queueapp.R;

public class UserFragment extends Fragment {

    EditText fname, lname, email, mobile;
    ImageView back;

    public UserFragment(){ }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        fname = (EditText) view.findViewById(R.id.fname);
        lname = (EditText) view.findViewById(R.id.lname);
        email = (EditText) view.findViewById(R.id.email);
        back = (ImageView) view.findViewById(R.id.back2);
        mobile = (EditText) view.findViewById(R.id.mobile);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainApp)getActivity()).setFragment(((MainApp)getActivity()).bottomNav.getSelectedItemId());
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("login", Context.MODE_PRIVATE);
        fname.setText(sharedPreferences.getString("keyfname", null));
        lname.setText(sharedPreferences.getString("keylname", null));
        email.setText(sharedPreferences.getString("keyemail", null));
        mobile.setText(sharedPreferences.getString("keymobile", null));

    }
}