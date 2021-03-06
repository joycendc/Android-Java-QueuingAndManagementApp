package com.oicen.queueapp.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
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
import com.oicen.queueapp.R;
import com.oicen.queueapp.models.UserModel;
import com.oicen.queueapp.utils.ApiHelper;
import com.oicen.queueapp.utils.DBHelper;
import com.oicen.queueapp.utils.SharedPrefManager;
import com.oicen.queueapp.utils.VolleySingleton;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.oicen.queueapp.BuildConfig.HOST;

public class MainActivity extends AppCompatActivity {
    final String LOGIN_URL = HOST + ApiHelper.LOGIN_URL;
    MaterialButton login;
    TextView signup;
    TextInputEditText mobileNumberField;
    DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //db = new DBHelper(this);
        try {
//            db.createDataBase();
//            db.openDataBase();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        setupTheme();
        if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES){
            setTheme(R.style.AppThemeDark);
        }
        else{ setTheme(R.style.AppTheme); }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (SharedPrefManager.getInstance(this).isLoggedIn()) {
            Intent intent = new Intent(getApplicationContext(), MainApp.class);
            startActivity(intent);
            finish();
        }

        getSupportActionBar().hide();
        mobileNumberField = findViewById(R.id.mobileNumber);
        login = findViewById(R.id.login);
        signup = findViewById(R.id.signup);


        login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mobileNumberField.onEditorAction(EditorInfo.IME_ACTION_DONE);
                userLogin();
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Registration.class);
                startActivity(intent);
            }
        });


        TextView tv = findViewById(R.id.txt);

        List<Pair<String, View.OnClickListener>> pairsList = new ArrayList<>();

        pairsList.add(new Pair<>("45,59", v -> {
            Intent intent = new Intent(this, Pptc.class);
            intent.putExtra("type", "pp");
            startActivity(intent);
        }));

        pairsList.add(new Pair<>("64,83", v -> {
            Intent intent = new Intent(this, Pptc.class);
            intent.putExtra("type", "tc");
            startActivity(intent);
        }));

        String pptc = "By logging in I agree to accept the KUMPARES Privacy Policy and Terms & Conditions.";

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


    void setupTheme(){
        SharedPreferences sp = getApplicationContext().getSharedPreferences("theme", Context.MODE_PRIVATE);
        boolean isDark = sp.getBoolean("isDark", false);
        System.out.println(isDark);
        if(isDark){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    void userLogin(){
        final String number = mobileNumberField.getText().toString();
        if(TextUtils.isEmpty(number)){
            mobileNumberField.setError("Please Enter your 11 digit Mobile Number!");
            mobileNumberField.requestFocus();
            return;
        }
        if(number.length() < 11){
            mobileNumberField.setError("Please Enter a 11 digit Mobile Number!");
            mobileNumberField.requestFocus();
            return;
        }


        StringRequest stringRequest = new StringRequest(Request.Method.POST, LOGIN_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONObject obj = new JSONObject(response);

                            if (!obj.getBoolean("error")) {
                                Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_LONG).show();

                                JSONObject userJson = obj.getJSONObject("user");

                                UserModel user = new UserModel(
                                        userJson.getString("id"),
                                        userJson.getString("fname"),
                                        userJson.getString("lname"),
                                        userJson.getString("mobile")
                                );


                                SharedPrefManager.getInstance(getApplicationContext()).userLogin(user);
                                finish();
                                startActivity(new Intent(getApplicationContext(), MainApp.class));
                            }else{
                                Toast.makeText(getApplicationContext(), obj.getString("message") + " else", Toast.LENGTH_LONG).show();
                            }

                        }catch (Exception e){
                            Log.e("volley", e.getMessage());

                            Toast.makeText(getApplicationContext(), "This number is not registered !", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.e("volley", volleyError.getMessage());
                        Toast.makeText(getApplicationContext(), "Please Connect to our Wifi first before using this app", Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("mobile", number);
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