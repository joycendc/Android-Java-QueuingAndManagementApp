package com.giligans.queueapp.activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
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
import androidx.core.content.ContextCompat;
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
import com.giligans.queueapp.R;
import com.giligans.queueapp.fragments.FavoritesFragment;
import com.giligans.queueapp.fragments.HomeFragment;
import com.giligans.queueapp.fragments.LineFragment;
import com.giligans.queueapp.fragments.PlateFragment;
import com.giligans.queueapp.fragments.ProducDetailsFragment;
import com.giligans.queueapp.fragments.RecentOrders;
import com.giligans.queueapp.fragments.TabCategoryFragment;
import com.giligans.queueapp.fragments.UserFragment;
import com.giligans.queueapp.models.QueueModel;
import com.giligans.queueapp.models.PlateModel;
import com.giligans.queueapp.network.QueueListener;
import com.giligans.queueapp.utils.DBHelper;
import com.giligans.queueapp.utils.SharedPrefManager;
import com.giligans.queueapp.utils.VolleySingleton;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.giligans.queueapp.BuildConfig.HOST;

public class MainApp extends AppCompatActivity {
    String ITEM_URL = HOST + "fetchitems.php";
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
    public ArrayList<QueueModel> customer;
    Handler handler;
    Runnable runnable;
    ProducDetailsFragment product;
    LineFragment line;
    public TabCategoryFragment tabcat;
    public FavoritesFragment home;
    public HomeFragment categories;
    public PlateFragment plate;
    ImageView settings, settings2;
    TextView timer, textView, greeting;
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
    public boolean guest;
    public String queueid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        inqueue = false;
        line = new LineFragment();
        home =  new FavoritesFragment();
        categories = new HomeFragment();
        plate = new PlateFragment();
        product = new ProducDetailsFragment();
        tabcat = new TabCategoryFragment();

        if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES){
            setTheme(R.style.AppThemeDark);
        }
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
    }

    public void openScanner(){
        IntentIntegrator intentIntegrator = new IntentIntegrator(MainApp.this);
        intentIntegrator.setPrompt("For flash use volume up key and volume down to turn it off ");
        intentIntegrator.setBeepEnabled(true);
        intentIntegrator.setOrientationLocked(true);
        intentIntegrator.setCaptureActivity(Scan.class);
        intentIntegrator.initiateScan();
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
                                String preptime = itemObject.getString("prep_time");
                                int cat_id = itemObject.getInt("cat_id");
                                //String url = HOST + "images/" + itemObject.getString("url");

                                dbHelper.saveItemsToLocalDB(id, name, description, price, preptime, cat_id, db);
                            }

                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
            VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
        }
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

    public int loadTheme(){
        SharedPreferences sharedPreferences = getApplication().getSharedPreferences("Theme", Context.MODE_PRIVATE);
        int theme = sharedPreferences.getInt("Theme", 1);
        return theme;
    }

    void saveTheme(int theme){
        SharedPreferences sharedPreferences = getApplication().getSharedPreferences("Theme", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("theme", theme);
        editor.apply();
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
                        connectivity = false;
                    }
                });
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
                fragmentClass = RecentOrders.class;
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

                            customer.add(new QueueModel(queue_id, id, name));
                        }
                        if (line.isAdded()) {
                            line.queueAdapter.update(customer);
                        }
                    } catch (JSONException e) { e.printStackTrace(); }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    getLine();
                }
            });

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
                    if(inqueue) line.pay.setVisibility(View.VISIBLE);
                }
                handler.postDelayed(this, 500);
            }
        }, 500);
    }

    public void orderDone(){
        final RequestQueue requestQueue = Volley.newRequestQueue(getApplication());
        final StringRequest stringRequest = new StringRequest(Request.Method.POST, GET_USER,
            new Response.Listener<String>(){
                @Override
                public void onResponse(String response){
                    if(!Boolean.parseBoolean(response)){
                        inqueue = true;
                        Intent serviceIntent = new Intent(getApplicationContext(), QueueListener.class);
                        serviceIntent.putExtra("inputExtra", "Test");
                        ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);
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
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setFragment(bottomNav.getSelectedItemId());
    }

    @Override
    protected void onStart() {
        super.onStart();
        orderDone();
        Bundle extras = getIntent().getExtras();
        String method = (extras != null) ? extras.getString("inline") : "";
        String checkGuest = (extras != null) ? extras.getString("type") : "";

        try {
            guest = checkGuest.equals("guest");
            if (method.equals("inline")) {
                inqueue = true;
                bottomNav.setSelectedItemId(R.id.navigation_line);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        timer.setVisibility(View.INVISIBLE);
        if(guest){
            settings.setVisibility(View.GONE);
            settings2.setVisibility(View.GONE);
            nvDrawer.setVisibility(View.GONE);
        }
    }
    public void setFragment(int item){
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
                selectedFragment).setTransition(FragmentTransaction.TRANSIT_NONE).commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
        new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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