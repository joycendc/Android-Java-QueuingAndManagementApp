package com.giligans.queueapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.giligans.queueapp.BuildConfig.HOST;

public class Registration extends AppCompatActivity {
    final String SIGNUP_URL = HOST + "register.php?apicall=signup";
    TextInputEditText fnameField, lnameField, emailField, mobileField, passField, passConfirmField;
    Button signup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES){ setTheme(R.style.AppThemeDark); }
        else{ setTheme(R.style.AppTheme); }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        getSupportActionBar().hide();

        signup = findViewById(R.id.signup);
        fnameField = findViewById(R.id.firstName);
        lnameField = findViewById(R.id.lastName);
        emailField = findViewById(R.id.emailField);
        mobileField = findViewById(R.id.mobile);
        passField = findViewById(R.id.passwordField);
        passConfirmField = findViewById(R.id.passwordFieldConfirm);

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });
    }

    void registerUser(){
        final String fname = fnameField.getText().toString();
        final String lname = lnameField.getText().toString();
        final String email = emailField.getText().toString();
        final String mobile = mobileField.getText().toString();
        final String password = passField.getText().toString();
        final String passConfirm = passConfirmField.getText().toString();

        if(TextUtils.isEmpty(fname)){
            fnameField.setError("Please Enter your First name !");
            fnameField.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(lname)){
            lnameField.setError("Please Enter your Last name !");
            lnameField.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(email)){
            emailField.setError("Please Enter your Email !");
            emailField.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(mobile)){
            mobileField.setError("Please Enter your Mobile Number !");
            mobileField.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(password)){
            passField.setError("Please Enter your Password!");
            passField.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(passConfirm)){
            passConfirmField.setError("Please Confirm your Password!");
            passConfirmField.requestFocus();
            return;
        }
        if(!password.equals(passConfirm)){
            passConfirmField.setError("Password didnt match !");
            passField.setError("Password didnt match !");
            return;
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, SIGNUP_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONObject obj = new JSONObject(response);

                            if (!obj.getBoolean("error")) {
                                Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_LONG).show();
                                finish();

                            }else{
                                Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_LONG).show();
                            }

                        }catch (Exception e){
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(getApplicationContext(), volleyError.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("fname", fname);
                params.put("lname", lname);
                params.put("mobile", mobile);
                params.put("email", email);
                params.put("password", password
                );
                return params;
            }
        };
        VolleySingelton.getInstance(this).addToRequestQueue(stringRequest);
    }
}