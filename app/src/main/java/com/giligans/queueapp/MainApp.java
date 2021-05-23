package com.giligans.queueapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
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
import com.giligans.queueapp.fragments.ProducDetailsFragment;
import com.giligans.queueapp.fragments.RecentOrders;
import com.giligans.queueapp.fragments.TabCategoryFragment;
import com.giligans.queueapp.fragments.UserFragment;
import com.giligans.queueapp.models.CustomerModel;
import com.giligans.queueapp.models.PlateModel;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static com.giligans.queueapp.BuildConfig.HOST;

public class MainApp extends AppCompatActivity {
    final String FETCH_URL = HOST + "fetchorders.php";
    String REMOVE = HOST + "orderdone.php";
    final String GET_TIME = HOST + "gettime.php";
    final String GET_USER = HOST + "getuser.php";
    final String INIT = HOST + "test.php";
    public long min = 60000;
    public long START_TIME_IN_MILLIS = 0;
    private CountDownTimer mCountDownTimer;
    private long mTimeLeftInMillis;
    private long mEndTime;
    public boolean mTimerRunning;
    public BottomNavigationView bottomNav;
    public ArrayList<CustomerModel> customer;
    Handler handler;
    Runnable runnable;
    ProducDetailsFragment product;
    LineFragment line;
    public TabCategoryFragment tabcat;
    public HomeFragment home;
    public CategoriesFragment categories;
    public PlateFragment plate;
    ImageView settings, settings2;
    TextView timer, textView;
    private DrawerLayout mDrawer;
    private NavigationView nvDrawer;
    SwitchCompat switchTheme;
    public boolean connectivity;
    ConstraintLayout top;
    boolean orderDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        line = new LineFragment();
        home =  new HomeFragment();
        categories = new CategoriesFragment();
        plate = new PlateFragment();
        product = new ProducDetailsFragment();
        tabcat = new TabCategoryFragment();


        if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES){ setTheme(R.style.AppThemeDark); }
        else{ setTheme(R.style.AppTheme); }
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_app);
        isConnected();

        top = (ConstraintLayout) findViewById(R.id.topBar);
        timer = (TextView) findViewById(R.id.timer);
        settings = (ImageView) findViewById(R.id.settings);
        textView = findViewById(R.id.textView);
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        nvDrawer = (NavigationView) findViewById(R.id.nvView);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator intentIntegrator = new IntentIntegrator(MainApp.this);
                intentIntegrator.setPrompt("For flash use volume up key and volume down to turn it off ");
                intentIntegrator.setBeepEnabled(true);
                intentIntegrator.setOrientationLocked(true);
                intentIntegrator.setCaptureActivity(Scan.class);
                intentIntegrator.initiateScan();
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawer.openDrawer(nvDrawer);
            }
        });

        switchTheme =  (SwitchCompat) nvDrawer.getMenu().getItem(2).getActionView().findViewById(R.id.switchTheme);
        settings2 = (ImageView) nvDrawer.getHeaderView(0).findViewById(R.id.settings);

        settings2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawer.closeDrawers();
            }
        });

        bottomNav = (BottomNavigationView) findViewById(R.id.navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("Notification", "Notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
        // Hide the activity toolbar
        getSupportActionBar().hide();

        setupDrawerContent(nvDrawer);

        //I added this if statement to keep the selected fragment when rotating the device
        if (savedInstanceState == null) { getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new CategoriesFragment()).commit(); }

        if(getItemCount() > 0){ setBadgeCount(getItemCount()); }
        customer = new ArrayList<CustomerModel>();
        bottomNav.setSelectedItemId(R.id.navigation_home);


        connectivity = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(intentResult.getContents() != null){
            String id= UUID.randomUUID().toString();
            String uid = id.substring(0, 5);
            new AlertDialog.Builder(this)
                .setTitle("Result")
                .setMessage(intentResult.getContents())
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
        }else{
            Toast.makeText(getApplicationContext(), "Please scan correctly !", Toast.LENGTH_SHORT).show();
        }
    }

    void changeTheme(){
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        this.recreate();
    }

    public boolean checkNetworkConnection(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public void isConnected() {
        if (checkNetworkConnection()) {
            StringRequest stringRequest = new StringRequest(Request.Method.GET, INIT,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if(response.equals("OK")) connectivity = true;
                            getLine();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            connectivity = false;
                            Toast.makeText(getApplicationContext(), error.getMessage() + " Server no response", Toast.LENGTH_SHORT).show();
                        }
                    });
            Volley.newRequestQueue(getApplicationContext()).add(stringRequest);
        }else{
            connectivity = false;
            Toast.makeText(getApplicationContext(),  "No Internet Connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        Fragment fragment = null;
        Class fragmentClass = null;
        switch(menuItem.getItemId()) {
            case R.id.nav_first_fragment:
                fragmentClass = UserFragment.class;
                break;
            case R.id.nav_second_fragment:
                fragmentClass = RecentOrders.class;
                break;
            case R.id.nav_third_fragment:
                if(!mTimerRunning) {
                    new AlertDialog.Builder(this)
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to logout ?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPrefManager.getInstance(getApplicationContext()).logout();
                                finish();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                }
                 break;
            case R.id.nav_switch:
                switchTheme.setChecked(switchTheme.isChecked() ? false : true);
                changeTheme();
                break;
            default:
                fragmentClass = null;
        }

        if(fragmentClass != null){
            try {
                fragment = (Fragment) fragmentClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Insert the fragment by replacing any existing fragment
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();
            // Highlight the selected item has been done by NavigationView
            menuItem.setChecked(false);
            // Set action bar title
            setTitle(menuItem.getTitle());
            // Close the navigation drawer
            mDrawer.closeDrawers();
        }
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
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, FETCH_URL,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONArray items = new JSONArray(response);
                        for (int i = 0; i < items.length(); i++) {
                            JSONObject itemObject = items.getJSONObject(i);

                            int id = itemObject.getInt("customer_id");
                            String name = itemObject.getString("customer_name");

                            customer.add(new CustomerModel(id+"", name));
                        }
                    } catch (JSONException e) { e.printStackTrace(); }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(), error.getMessage() + " line no response", Toast.LENGTH_SHORT).show();
                    return;
                }
            });


            handler = new Handler();
            runnable = new Runnable() {
                @Override
                public void run() {
                    requestQueue.add(stringRequest);

                    if (line.isAdded()) { line.customerAdapter.setData(customer); }

                    handler.postDelayed(this, 500);
                    customer = new ArrayList<CustomerModel>();
                }
            };
            handler.post(runnable);

    }

    void finishNa(){
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear().apply();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, REMOVE,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (!obj.getBoolean("error")) {
                            Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(), error.getMessage() + " finish", Toast.LENGTH_SHORT).show();
                }
            }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("login", Context.MODE_PRIVATE);
                String id = sharedPreferences.getString("keyid", null);
                params.put("customer_id", id);
                return params;
            }
        };
        Volley.newRequestQueue(getApplicationContext()).add(stringRequest);
    }

    public void getTime(){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, GET_TIME,
            new Response.Listener<String>(){
                @Override
                public void onResponse(String response){
                    try {
                        mTimeLeftInMillis = Integer.parseInt(response) > 0 ? Integer.parseInt(response) * min : 1 * min;
                    } catch (Exception e) {
                        Log.e("TIME", e.getMessage());
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error){
                    Toast.makeText(getApplicationContext(), error.getMessage() + "time", Toast.LENGTH_SHORT).show();
                }
            }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("login", Context.MODE_PRIVATE);
                String id = sharedPreferences.getString("keyid", null);
                params.put("customer_id", id);
                return params;
            }
        };
        Volley.newRequestQueue(getApplicationContext()).add(stringRequest);
    }

    void orderDone(){
        final RequestQueue requestQueue = Volley.newRequestQueue(getApplication());
        final StringRequest stringRequest = new StringRequest(Request.Method.POST, GET_USER,
            new Response.Listener<String>(){
                @Override
                public void onResponse(String response){
                    try {
                        orderDone = Boolean.parseBoolean(response);
                    } catch (Exception e) {
                        Log.e("TIME", e.getMessage());
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error){
                    orderDone = false;
                    Toast.makeText(getApplicationContext(), error.getMessage() + "time", Toast.LENGTH_SHORT).show();
                }
            }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("login", Context.MODE_PRIVATE);
                String id = sharedPreferences.getString("keyid", null);
                params.put("customer_id", id);
                return params;
            }
        };
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                requestQueue.add(stringRequest);

                if(orderDone){
                    handler.removeCallbacks(runnable);
                    timer.setVisibility(View.INVISIBLE);
                    mCountDownTimer.cancel();
                    mTimerRunning = false;
                    mTimeLeftInMillis = 0;
                    SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear().apply();
                    updateCountDownText();
                }
                handler.postDelayed(this, 100);
            }
        };
        handler.post(runnable);
    }

    public void startTimer(){
        orderDone();
        timer.setVisibility(View.VISIBLE);
        mEndTime = System.currentTimeMillis() + mTimeLeftInMillis;
        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                if(mTimeLeftInMillis + 500 > 30000 && mTimeLeftInMillis - 500 < 30000){
                    String message = "30s Left";
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(MainApp.this, "Notification");
                    builder.setSmallIcon(R.drawable.ic_fastfood_24dp);
                    builder.setContentTitle("30s Left !");
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {  v.vibrate(VibrationEffect.createOneShot(1500, VibrationEffect.DEFAULT_AMPLITUDE)); }
                    else { v.vibrate(500); }
                }
                updateCountDownText();
            }
            @Override
            public void onFinish() {
                timer.setVisibility(View.INVISIBLE);
                mTimerRunning = false;
                mTimeLeftInMillis = 0;
                finishNa();
                Toast.makeText(getApplicationContext(), "YOU'RE NEXT IN LINE", Toast.LENGTH_LONG).show();
                String message = "Please go to the counter now";
                NotificationCompat.Builder builder = new NotificationCompat.Builder(MainApp.this, "Notification");
                builder.setSmallIcon(R.drawable.ic_fastfood_24dp);
                builder.setContentTitle("Your order is ready !");
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {  v.vibrate(VibrationEffect.createOneShot(1500, VibrationEffect.DEFAULT_AMPLITUDE)); }
                else { v.vibrate(1500); }
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
        if(mTimerRunning) {
            SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong("millisLeft", mTimeLeftInMillis);
            editor.putBoolean("timerRunning", mTimerRunning);
            editor.putLong("endTime", mEndTime);
            editor.apply();
        }
        if (mCountDownTimer != null) { mCountDownTimer.cancel(); }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setFragment(bottomNav.getSelectedItemId());
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        mTimeLeftInMillis = prefs.getLong("millisLeft", 0);
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
            }
            else {
                startTimer();
            }
        }else{
            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
        }
    }
    public void setFragment(int item){
        Fragment selectedFragment = null;
        switch (item) {
            case R.id.navigation_home:
                selectedFragment = new HomeFragment();
                break;
            case R.id.navigation_dashboard:
                selectedFragment = new CategoriesFragment();
                break;
            case R.id.navigation_notifications:
                selectedFragment = new PlateFragment();
                break;
            case R.id.navigation_line:
                selectedFragment = line;
                break;
            default:
                break;
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                selectedFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
        new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        selectedFragment = new HomeFragment();
                        break;
                    case R.id.navigation_dashboard:
                        selectedFragment = new CategoriesFragment();
                        break;
                    case R.id.navigation_notifications:
                        selectedFragment = new PlateFragment();
                        break;
                    case R.id.navigation_line:
                        selectedFragment = line;
                        break;
                    default:
                        selectedFragment = new HomeFragment();
                        break;
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        selectedFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
                return true;
            }
        };

    private Fragment getVisibleFragment() {
        FragmentManager fragmentManager = MainApp.this.getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        for (Fragment fragment : fragments) {
            if (fragment != null && fragment.isVisible())
                return fragment;
        }
        return null;
    }

    @Override
    public void onBackPressed() {
        if (bottomNav.getSelectedItemId() == R.id.navigation_home) {
            if (getVisibleFragment() instanceof HomeFragment) {
                finish();
            }else{
                setFragment(R.id.navigation_home);
            }
        }
        else {
            bottomNav.setSelectedItemId(R.id.navigation_home);
            setFragment(R.id.navigation_home);
        }
    }
}