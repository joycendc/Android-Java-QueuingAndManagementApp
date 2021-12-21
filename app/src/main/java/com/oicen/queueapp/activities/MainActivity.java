package com.oicen.queueapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.android.volley.AuthFailureError;
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
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    final String LOGIN_URL = ApiHelper.LOGIN_URL;
    MaterialButton login;
    TextView signup;
    TextInputEditText mobileNumberField;
    DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        db = new DBHelper(this);
        try {
//            db.createDataBase();
//            db.openDataBase();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES){ setTheme(R.style.AppThemeDark); }
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
    }

    void userLogin(){
        final String number = mobileNumberField.getText().toString();
        if(TextUtils.isEmpty(number)){
            mobileNumberField.setError("Please Enter your Mobile Number !");
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
                            Toast.makeText(getApplicationContext(), e.getMessage() + " catch", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(getApplicationContext(), "Please Connect to our Wifi first before using this app", Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("mobile", number);
                return params;
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }
}