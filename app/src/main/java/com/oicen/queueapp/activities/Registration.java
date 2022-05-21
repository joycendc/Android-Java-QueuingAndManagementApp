package com.oicen.queueapp.activities;

import static com.oicen.queueapp.BuildConfig.HOST;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.textfield.TextInputEditText;
import com.oicen.queueapp.R;
import com.oicen.queueapp.utils.ApiHelper;
import com.oicen.queueapp.utils.VolleySingleton;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Registration extends AppCompatActivity {
    final String SIGNUP_URL = HOST + ApiHelper.SIGNUP_URL;
    TextInputEditText fnameField, lnameField, confirmField, mobileField;
    Button signup;
    ImageView back;

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
        mobileField = findViewById(R.id.mobileNumber);
        confirmField = findViewById(R.id.confirmNumber);
        back = findViewById(R.id.back);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });

        TextView tv = findViewById(R.id.txt);

        List<Pair<String, View.OnClickListener>> pairsList = new ArrayList<>();

        pairsList.add(new Pair<>("49,63", v -> {
            Intent intent = new Intent(this, Pptc.class);
            intent.putExtra("type", "pp");
            startActivity(intent);
        }));

        pairsList.add(new Pair<>("68,87", v -> {
            Intent intent = new Intent(this, Pptc.class);
            intent.putExtra("type", "tc");
            startActivity(intent);
        }));

        String pptc = "By tapping Signup I agree to accept the KUMPARES Privacy Policy and Terms & Conditions.";

        makeLinks(pairsList, pptc, tv);
    }

    private void makeLinks(List<Pair<String, View.OnClickListener>> pairsList, String text, TextView tv) {
        SpannableString ss = new SpannableString(text);
        for (Pair<String, View.OnClickListener> pair : pairsList) {

            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    pair.second.onClick(textView);
                }

                @Override
                public void updateDrawState(TextPaint ds) {

                    ds.linkColor = ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary);
                    ds.setUnderlineText(true);

                    super.updateDrawState(ds);
                }
            };

            String[] indexes = pair.first.split(",");
            ss.setSpan(clickableSpan, Integer.parseInt(indexes[0]), Integer.parseInt(indexes[1]), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        tv.setText(ss);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
    }

    void registerUser(){
        final String fname = fnameField.getText().toString();
        final String lname = lnameField.getText().toString();
        final String mobile = mobileField.getText().toString();
        final String confirm = confirmField.getText().toString();

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
        if(TextUtils.isEmpty(mobile)){
            mobileField.setError("Please Enter your  11 digit Mobile Number !");
            mobileField.requestFocus();
            return;
        }

        if(mobile.length() < 11){
            mobileField.setError("Please Enter a 11 digit Mobile Number!");
            mobileField.requestFocus();
            return;
        }

        if(TextUtils.isEmpty(confirm)){
            confirmField.setError("Please Confirm your Mobile Number !");
            confirmField.requestFocus();
            return;
        }


        if(!mobile.equals(confirm)){
            mobileField.setError("Password didnt match !");
            confirmField.setError("Password didnt match !");
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
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }
}