package com.giligans.queueapp.network;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.giligans.queueapp.activities.MainApp;
import com.giligans.queueapp.R;
import com.giligans.queueapp.models.QueueModel;
import com.giligans.queueapp.utils.VolleySingleton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.app.NotificationManager.IMPORTANCE_HIGH;
import static com.giligans.queueapp.BuildConfig.HOST;

public class QueueListener extends Service {
    final String GET_USER = HOST + "getuser.php";
    boolean orderDone;
    Context context;
    public ArrayList<QueueModel> customer;
    Handler handler;
    Runnable runnable;
    boolean queued;
    boolean isStarted = false;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!isStarted) {
            isStarted = true;

            String CHANNEL_ONE_ID = "com.giligans.queueapp.N1";
            String CHANNEL_ONE_NAME = "Channel One";
            NotificationChannel notificationChannel = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                notificationChannel = new NotificationChannel(CHANNEL_ONE_ID,
                        CHANNEL_ONE_NAME, IMPORTANCE_HIGH);
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.setShowBadge(true);
                notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                manager.createNotificationChannel(notificationChannel);
            }

            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            Notification notification = new Notification.Builder(context)
                    .setChannelId(CHANNEL_ONE_ID)
                    .setContentTitle("You are in Line")
                    .setContentText("Please be alert")
                    .setSmallIcon(R.drawable.ic_line_24dp)
                    .setLargeIcon(icon)
                    .build();

            Intent notificationIntent = new Intent(context, MainApp.class);
            notificationIntent.putExtra("inline","inline");
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            notification.contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            startForeground(1, notification);

            queued = false;

            final RequestQueue requestQueue = Volley.newRequestQueue(getApplication());
            final StringRequest stringRequest = new StringRequest(Request.Method.POST, GET_USER,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                orderDone = Boolean.parseBoolean(response);
                            } catch (Exception e) {
                                Log.e("TIME", e.getMessage());
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            orderDone = false;
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
            };
            handler = new Handler();
            runnable = new Runnable() {
                @Override
                public void run() {
                    VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);

                    if (orderDone) {
                        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        // Vibrate for 1000 milliseconds
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            v.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.EFFECT_DOUBLE_CLICK));
                        } else {
                            v.vibrate(1000);
                        }

                        stopForeground(true);
                        stopSelfResult(startId);
                        String message = "Please go to the counter now";
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "Notification");
                        builder.setSmallIcon(R.drawable.ic_fastfood_24dp);
                        builder.setContentTitle("Your order is ready !");
                        builder.setContentText(message);
                        builder.setAutoCancel(true);

                        Intent i = new Intent(getApplicationContext(), MainApp.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        //i.putExtra("message", message);
                        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
                        builder.setContentIntent(pendingIntent);

                        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(getApplicationContext());
                        managerCompat.notify(1, builder.build());

                        Intent newIntent = new Intent(getApplicationContext(), MainApp.class);
                        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(newIntent);
                    }
                    if (!orderDone) handler.postDelayed(this, 100);
                }
            };
            handler.post(runnable);
        }

        return START_STICKY;
//        String input = intent.getStringExtra("inputExtra");
//        Intent notificationIntent = new Intent(this, MainApp.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this,
//                0, notificationIntent, 0);
//        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
//                .setContentTitle("Example Service")
//                .setContentText(input)
//                .setSmallIcon(R.drawable.ic_cart_24dp)
//                .setContentIntent(pendingIntent)
//                .build();
//        startForeground(1, notification);
//        //do heavy work on a background thread
//        //stopSelf();
//        return START_NOT_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}