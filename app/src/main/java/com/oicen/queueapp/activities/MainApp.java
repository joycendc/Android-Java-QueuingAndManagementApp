package com.oicen.queueapp.activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.oicen.queueapp.R;
import com.oicen.queueapp.fragments.FavoritesFragment;
import com.oicen.queueapp.fragments.HomeFragment;
import com.oicen.queueapp.fragments.PlateFragment;
import com.oicen.queueapp.fragments.ProducDetailsFragment;
import com.oicen.queueapp.fragments.QueueFragment;
import com.oicen.queueapp.fragments.RecentOrdersFragment;
import com.oicen.queueapp.fragments.TabCategoryFragment;
import com.oicen.queueapp.fragments.UserFragment;
import com.oicen.queueapp.models.PlateModel;
import com.oicen.queueapp.models.QueueModel;
import com.oicen.queueapp.network.QueueListener;
import com.oicen.queueapp.utils.ApiHelper;
import com.oicen.queueapp.utils.DBHelper;
import com.oicen.queueapp.utils.SharedPrefManager;
import com.oicen.queueapp.utils.VolleySingleton;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.oicen.queueapp.BuildConfig.HOST;

public class MainApp extends AppCompatActivity {
    String ITEM_URL = HOST + ApiHelper.ITEM_URL;
    final String FETCH_URL = HOST + ApiHelper.FETCH_URL;
    final String GET_USER = HOST + ApiHelper.GET_USER;
    final String INIT = HOST + ApiHelper.INIT;
    public BottomNavigationView bottomNav;
    public ArrayList<QueueModel> customer;
    ProducDetailsFragment product;
    QueueFragment line;
    public TabCategoryFragment tabcat;
    public FavoritesFragment home;
    public HomeFragment categories;
    public PlateFragment plate;
    ImageView settings, settings2;
    TextView textView, greeting;    
    private DrawerLayout mDrawer;
    private NavigationView nvDrawer;
    SwitchCompat switchTheme;
    public boolean connectivity;
    ConstraintLayout top;
    boolean orderDone;
    AlertDialog.Builder resDialog;
    public int tabPos;
    DBHelper dbHelper;
    SQLiteDatabase db;
    public boolean inqueue;
    public String queueid;
    public boolean isPaid;
    TextView pp,tc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        inqueue = false;
        isPaid = false;
        line = new QueueFragment();
        home =  new FavoritesFragment();
        categories = new HomeFragment();
        plate = new PlateFragment();
        product = new ProducDetailsFragment();
        tabcat = new TabCategoryFragment();

        setupTheme();
        if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES){
            setTheme(R.style.AppThemeDark);
        }
        else{ setTheme(R.style.AppTheme); }
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_app);

        isConnected();

        top = (ConstraintLayout) findViewById(R.id.topBar);

        settings = (ImageView) findViewById(R.id.settings);
        textView = findViewById(R.id.textView);
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        nvDrawer = (NavigationView) findViewById(R.id.nvView);

        pp = (TextView)findViewById(R.id.pp);
        tc = (TextView)findViewById(R.id.tc);


        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               
                mDrawer.openDrawer(nvDrawer);
            }
        });

        pp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Pptc.class);
                intent.putExtra("type", "pp");
                startActivity(intent);
            }
        });

        tc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Pptc.class);
                intent.putExtra("type", "tc");
                startActivity(intent);
            }
        });

        switchTheme = (SwitchCompat) nvDrawer.getMenu().getItem(2).getActionView().findViewById(R.id.switchTheme);

        settings2 = (ImageView) nvDrawer.getHeaderView(0).findViewById(R.id.settings);

        greeting = (TextView) nvDrawer.getHeaderView(0).findViewById(R.id.greeting);
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("login", Context.MODE_PRIVATE);
        greeting.setText("Hello, " + sharedPreferences.getString("keyfname", null));

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
        Objects.requireNonNull(getSupportActionBar()).hide();

        setupDrawerContent(nvDrawer);

        //I added this if statement to keep the selected fragment when rotating the device
        if (savedInstanceState == null) { getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit(); }

        if(getItemCount() > 0){ setBadgeCount(getItemCount()); }
        customer = new ArrayList<QueueModel>();
        bottomNav.setSelectedItemId(R.id.navigation_dashboard);

        connectivity = false;

        if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES){
            switchTheme.setChecked(true);
        }

        tabPos = 0;

        resDialog =  new AlertDialog.Builder(this);
        dbHelper = new DBHelper(getApplicationContext());
        fetchItemsFromServer();
        orderPaid();


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

    public void showMessage(String text){
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
    }

    void fetchItemsFromServer(){
        if (checkNetworkConnection()) {
            db = dbHelper.getWritableDatabase();
            StringRequest stringRequest = new StringRequest(Request.Method.GET, ITEM_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            db.execSQL(dbHelper.DROP_ITEM_TABLE);
                            db.execSQL(dbHelper.CREATE_ITEM_TABLE);
                            JSONArray items = new JSONArray(response);

                            for (int i = 0; i < items.length(); i++) {
                                JSONObject itemObject = items.getJSONObject(i);

                                int id = itemObject.getInt("id");
                                String name = itemObject.getString("name");
                                String description = itemObject.getString("description");
                                String price = itemObject.getString("price");
                                int cat_id = itemObject.getInt("cat_id");
                                //String url = HOST + "images/" + itemObject.getString("url");

                                dbHelper.saveItemsToLocalDB(id, name, description, price, cat_id, db);
                            }
                            db.close();

                        } catch (JSONException e) {
                            showMessage("serverFetchErr" + e.getMessage());
                            db.close();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        db.close();
                    }
                }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put(ApiHelper.KEY_COOKIE, ApiHelper.VALUE_CONTENT);
                    return headers;
                }
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0, -1,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
        }
    }

    void changeTheme(){
        SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences("theme", Context.MODE_PRIVATE).edit();
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            editor.putBoolean("isDark", false).apply();
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            editor.putBoolean("isDark", true).apply();
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
                        if(response.equals("OK")){
                            connectivity = true;
                        }else{
                            resDialog
                                .setTitle("Warning")
                                .setMessage("Please connect to our wifi first to use this app or else you can only see the list of menus")
                                .setPositiveButton("CONNECT", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                                        recreate();
                                    }
                                })
                                .setNegativeButton("CONTINUE", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setCancelable(false)
                                .show();
                        }
                        getLine();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        connectivity = false;
                        isConnected();

                    }
                }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put(ApiHelper.KEY_COOKIE, ApiHelper.VALUE_CONTENT);
                    return headers;
                }
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0, -1,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
        }else{
            new AlertDialog.Builder(this)
                .setTitle("Warning")
                .setMessage("Please connect to our wifi named \"Giligans\" first to use this app or else you can only see the list of menus")
                .setPositiveButton("CONNECT", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        recreate();

                    }
                })
                .setNegativeButton("CONTINUE", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false)
                .show();
            connectivity = false;
        }
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
            new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NotNull MenuItem menuItem) {
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
                fragmentClass = RecentOrdersFragment.class;
                break;
            case R.id.nav_third_fragment:
                if(!inqueue) {
                    new AlertDialog.Builder(this)
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to logout ?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPrefManager.getInstance(getApplicationContext()).logout();
                                finish();
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                }
                break;
            case R.id.nav_switch:
                switchTheme.setChecked(!switchTheme.isChecked());

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

    public void increaseBadgeCount(){
        int menuItemId = bottomNav.getMenu().getItem(2).getItemId();
        BadgeDrawable badge = bottomNav.getOrCreateBadge(menuItemId);
        badge.setNumber(badge.getNumber() + 1);
    }

    public void dereaseBadgeCount(){
        int menuItemId = bottomNav.getMenu().getItem(2).getItemId();
        BadgeDrawable badge = bottomNav.getOrCreateBadge(menuItemId);
        badge.setNumber(badge.getNumber() - 1);
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
         final StringRequest stringRequest = new StringRequest(Request.Method.GET, FETCH_URL,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    customer = new ArrayList<QueueModel>();
                    try {
                        JSONArray items = new JSONArray(response);
                        for (int i = 0; i < items.length(); i++) {
                            JSONObject itemObject = items.getJSONObject(i);

                            String queue_id = itemObject.getString("queue_id");
                            String id = itemObject.getString("customer_id");
                            String name = itemObject.getString("customer_name");
                            String status = itemObject.getString("status");

                            customer.add(new QueueModel(queue_id, id, name, status));
                        }
                        if (line.isAdded()) {
                            line.queueAdapter.update(customer);
                            line.shimmerFrameLayout.startShimmer();
                            line.shimmerFrameLayout.setVisibility(View.GONE);
                            line.customerRecycler.setVisibility(View.VISIBLE);
                        }
                    } catch (JSONException e) { e.printStackTrace(); }

                }
            },
            new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                getLine();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put(ApiHelper.KEY_COOKIE, ApiHelper.VALUE_CONTENT);
                return headers;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                0, -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);

                if (line.isAdded()) {
                    if(line.queueAdapter.getItemCount() == 0){
                        line.empty.setVisibility(View.VISIBLE);

                    }else{
                        line.empty.setVisibility(View.INVISIBLE);
                    }
                    if(inqueue && !isPaid) line.pay.setVisibility(View.VISIBLE);
                }
                handler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    public void orderDone(){
        final RequestQueue requestQueue = Volley.newRequestQueue(getApplication());
        final StringRequest stringRequest = new StringRequest(Request.Method.POST, GET_USER,
            new Response.Listener<String>(){
                @Override
                public void onResponse(String isInQueue){
                    if(Boolean.parseBoolean(isInQueue)){
                        inqueue = true;
                        bottomNav.setSelectedItemId(R.id.navigation_line);
                        Intent serviceIntent = new Intent(getApplicationContext(), QueueListener.class);
                        serviceIntent.putExtra("inputExtra", "Test");
                        serviceIntent.setAction("START_MAIN");
                        ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error){
                    inqueue = false;
                    showMessage("orderDoneFetchErr" + error.getMessage());
                }
            }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("login", Context.MODE_PRIVATE);
                String id = sharedPreferences.getString("keyid", null);
                params.put("customer_id", id);
                params.put("queue", "queueListener");
                return params;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put(ApiHelper.KEY_COOKIE, ApiHelper.VALUE_CONTENT);
                return headers;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                0, -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }

    public void orderPaid(){
        final RequestQueue requestQueue = Volley.newRequestQueue(getApplication());
        final StringRequest stringRequest = new StringRequest(Request.Method.POST, GET_USER,
            new Response.Listener<String>(){
                @Override
                public void onResponse(String response){
                    isPaid = Boolean.parseBoolean(response);
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error){
                    isPaid = false;
                    showMessage("orderDoneFetchErr" + error.getMessage());
                }
            }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("login", Context.MODE_PRIVATE);
                String id = sharedPreferences.getString("keyid", null);
                params.put("customer_id", id);
                params.put("paid", "paymentListener");
                return params;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put(ApiHelper.KEY_COOKIE, ApiHelper.VALUE_CONTENT);
                return headers;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                0, -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setFragment(bottomNav.getSelectedItemId());
        orderPaid();
        displayMessage();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Bundle extras = getIntent().getExtras();
        if(extras == null){
            orderDone();
        }
        orderPaid();
        displayMessage();
    }

    void displayMessage(){
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            if(extras.getBoolean("done")){
                resDialog
                    .setTitle("Your order is ready !")
                    .setMessage("PLEASE GO TO THE COUNTER NOW TO CLAIM YOUR ORDER.")
                    .setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setCancelable(false)
                    .show();
            }
        }
    }

    public synchronized void setFragment(int item){
        Fragment selectedFragment = null;
        switch (item) {
            case R.id.navigation_home:
                selectedFragment = new FavoritesFragment();
                break;
            case R.id.navigation_dashboard:
                selectedFragment = new HomeFragment(tabPos);
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
                selectedFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE).commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
        new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public synchronized boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        selectedFragment = new FavoritesFragment();
                        break;
                    case R.id.navigation_dashboard:
                        selectedFragment = new HomeFragment();
                        break;
                    case R.id.navigation_notifications:
                        selectedFragment = new PlateFragment();
                        break;
                    case R.id.navigation_line:
                        selectedFragment = line;
                        break;
                    default:
                        selectedFragment = new FavoritesFragment();
                        break;
                }
                if (!getSupportFragmentManager().isDestroyed()) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            selectedFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
                }
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
        if (bottomNav.getSelectedItemId() == R.id.navigation_dashboard) {
            if (getVisibleFragment() instanceof HomeFragment) {
                if(tabPos == 0) {
                    finish();
                }else{
                    tabPos = 0;
                    setFragment(R.id.navigation_dashboard);
                }
            }else{
                setFragment(R.id.navigation_dashboard);
            }
        }
        else {
            if (getVisibleFragment() instanceof ProducDetailsFragment) {
                bottomNav.setSelectedItemId(bottomNav.getSelectedItemId());
                setFragment(bottomNav.getSelectedItemId());
            }else {
                bottomNav.setSelectedItemId(R.id.navigation_dashboard);
                setFragment(R.id.navigation_dashboard);
            }
        }
    }
}
