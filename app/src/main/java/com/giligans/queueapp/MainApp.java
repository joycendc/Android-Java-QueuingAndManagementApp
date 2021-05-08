package com.giligans.queueapp;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.giligans.queueapp.fragments.CategoriesFragment;
import com.giligans.queueapp.fragments.HomeFragment;
import com.giligans.queueapp.fragments.LineFragment;
import com.giligans.queueapp.fragments.PlateFragment;
import com.giligans.queueapp.models.CustomerModel;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.giligans.queueapp.models.PlateModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Locale;

public class MainApp extends AppCompatActivity {

    TextView timer, textView;
    private final long START_TIME_IN_MILLIS = 30000;
    private CountDownTimer mCountDownTimer;
    private long mTimeLeftInMillis;
    private long mEndTime;
    public boolean mTimerRunning;
    public BottomNavigationView bottomNav;

    public ArrayList<CustomerModel> customer;
    Handler handler;
    Runnable runnable;
    LineFragment line;
    HomeFragment home;
    CategoriesFragment categories;
    PlateFragment plate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES){
            setTheme(R.style.AppThemeLight);
        }else{
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_app);

        timer = (TextView) findViewById(R.id.timer);
        textView = (TextView) findViewById(R.id.textView);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    restart();
                }else{
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    restart();
                }

            }
        });

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("Notification", "Notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
        // Hide the activity toolbar
        getSupportActionBar().hide();

        bottomNav = (BottomNavigationView) findViewById(R.id.navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        //I added this if statement to keep the selected fragment when rotating the device
        if (savedInstanceState == null) { getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new CategoriesFragment()).commit(); }


        if(getItemCount() > 0) setBadgeCount(getItemCount());
        customer = new ArrayList<CustomerModel>();
        line = new LineFragment();
        home =  new HomeFragment();
        categories = new CategoriesFragment();
        plate = new PlateFragment();
        bottomNav.setSelectedItemId(R.id.navigation_home);

       getLine();
    }

    void restart(){
        Intent intent = new Intent(getApplicationContext(), MainApp.class);
        startActivity(intent);
        finish();
    }

    public void setBadgeCount(int count){
        int menuItemId = bottomNav.getMenu().getItem(2).getItemId();
        BadgeDrawable badge = bottomNav.getOrCreateBadge(menuItemId);
        badge.setNumber(count);
    }

    public int getItemCount(){
        int count = 0;
        SharedPreferences sp = getSharedPreferences("plate_list", Context.MODE_PRIVATE);
        String json = sp.getString("orderlist", null);
        if(json != null){
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<PlateModel>>() {}.getType();
            ArrayList<PlateModel> newList = new ArrayList<PlateModel>();
            newList = gson.fromJson(json, type);
            count = newList.size();
        }
        return count;
    }


    public void getLine(){
        final RequestQueue requestQueue = Volley.newRequestQueue(getApplication());
        final String FETCH_URL = "http://192.168.254.152/fetchorders.php";
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, FETCH_URL,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    ArrayList<PlateModel> test = new ArrayList<>();
                    test.add(new PlateModel("ADOBO", "100", 1));
                    test.add(new PlateModel("TINOLA", "80", 1));
                    try {
                        JSONArray items = new JSONArray(response);
                        for (int i = 0; i < items.length(); i++) {
                            JSONObject itemObject = items.getJSONObject(i);

                            String id = itemObject.getString("id");
                            String cust = itemObject.getString("customer");
                            String orderlist = itemObject.getString("orderlist");
                            String total = itemObject.getString("total");

                            customer.add(new CustomerModel(id, total, cust, test));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                requestQueue.add(stringRequest);

                if(line.isAdded()){
                    ((LineFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_line)).customerAdapter.updateList(customer);
                }else{
                    ((LineFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_line)).setCustomerRecycler(customer);
                }
                    //line.setCustomerRecycler(customer);
                customer = new ArrayList<CustomerModel>();
                handler.postDelayed(this, 500);
            }
        };
        handler.post(runnable);
    }

    void finishNa(){
        final String REMOVE = "http://192.168.254.152/orderdone.php";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, REMOVE,
            new Response.Listener<String>(){
                @Override
                public void onResponse(String response){
                    try {
                        Toast.makeText(getApplicationContext().getApplicationContext(), "FINISHED", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error){
                    Toast.makeText(getApplicationContext().getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        Volley.newRequestQueue(getApplication().getApplicationContext()).add(stringRequest);
    }

    public void startTimer(){
        timer.setVisibility(View.VISIBLE);
        mEndTime = System.currentTimeMillis() + mTimeLeftInMillis;
        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }
            @Override
            public void onFinish() {
                timer.setVisibility(View.INVISIBLE);
                mTimerRunning = false;
                mTimeLeftInMillis = START_TIME_IN_MILLIS;
                finishNa();
                String message = "Notif Test";
                NotificationCompat.Builder builder = new NotificationCompat.Builder(MainApp.this, "Notification");
                builder.setSmallIcon(R.drawable.ic_fastfood_24dp);
                builder.setContentTitle("New Notif");
                builder.setContentText(message);
                builder.setAutoCancel(true);

                Intent i = new Intent(MainApp.this, Notification.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.putExtra("message", message);
                PendingIntent pendingIntent =  PendingIntent.getActivity(MainApp.this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(pendingIntent);

                NotificationManagerCompat managerCompat = NotificationManagerCompat.from(MainApp.this);
                managerCompat.notify(1, builder.build());
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                // Vibrate for 500 milliseconds
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(1500, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    //deprecated in API 26
                    v.vibrate(1500);
                }

               // timer.setText("ATTENTION: YOU ARE NEXT IN THE LINE");
            }
        }.start();
        mTimerRunning = true;
    }

    private void updateCountDownText() {
        int minutes = (int) (mTimeLeftInMillis / 1000) / 60;
        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;
        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        timer.setText(timeLeftFormatted);
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("millisLeft", mTimeLeftInMillis);
        editor.putBoolean("timerRunning", mTimerRunning);
        editor.putLong("endTime", mEndTime);
        editor.apply();
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        mTimeLeftInMillis = prefs.getLong("millisLeft", START_TIME_IN_MILLIS);
        mTimerRunning = prefs.getBoolean("timerRunning", false);
        updateCountDownText();
        timer.setVisibility(View.INVISIBLE);
        if (mTimerRunning) {
            mEndTime = prefs.getLong("endTime", 0);
            mTimeLeftInMillis = mEndTime - System.currentTimeMillis();
            if (mTimeLeftInMillis < 0) {
                mTimeLeftInMillis = 0;
                mTimerRunning = false;
                mTimeLeftInMillis = START_TIME_IN_MILLIS;
                updateCountDownText();
            } else {
                startTimer();
            }
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
        new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        selectedFragment = home;
                        break;
                    case R.id.navigation_dashboard:
                        selectedFragment = categories;
                        break;
                    case R.id.navigation_notifications:
                        selectedFragment = plate;
                        break;
                    case R.id.navigation_line:
                        selectedFragment = line;
                        break;
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        selectedFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
                return true;
            }
        };

    @Override
    public void onBackPressed() {
        if (bottomNav.getSelectedItemId() == R.id.navigation_home) {
            super.onBackPressed();
        } else {
            bottomNav.setSelectedItemId(R.id.navigation_home);
        }
    }
}