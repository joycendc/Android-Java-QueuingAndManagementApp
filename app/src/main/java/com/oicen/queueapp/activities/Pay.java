package com.oicen.queueapp.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.oicen.queueapp.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.oicen.queueapp.models.QueueModel;
import com.oicen.queueapp.utils.ApiHelper;
import com.oicen.queueapp.utils.VolleySingleton;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

import static com.oicen.queueapp.BuildConfig.HOST;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Pay extends AppCompatActivity {
    final String CHECK_PAID = HOST + ApiHelper.GET_USER;
    ImageView qr;
    Boolean isPaid = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);

        getSupportActionBar().hide();

        Bundle extras = getIntent().getExtras();
        String method = extras.getString("qr");
        String code = !method.equals(null) ? method : "";

        qr = (ImageView) findViewById(R.id.imageView2);
        try {
            qr.setImageBitmap(generateQrCode(code));
        } catch (WriterException e) {
            e.printStackTrace();
        }

        checkPaid();
    }

    public static Bitmap generateQrCode(String myCodeText) throws WriterException {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(myCodeText, BarcodeFormat.QR_CODE, 500, 500);
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = bitMatrix.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }
    
    public void checkPaid(){
        final StringRequest stringRequest = new StringRequest(Request.Method.POST, CHECK_PAID,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            isPaid = Boolean.parseBoolean(response);

                        } catch (Exception e) {
                            e.printStackTrace(); }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
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
        };

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);

                if(isPaid){
                    Intent i = new Intent();
                    i.putExtra("result", "Payment Success!");
                    setResult(RESULT_OK, i);
                    finish();
                }
                handler.postDelayed(this, 500);
            }
        }, 500);
    }


    @Override
    public void onBackPressed() {
        Intent i = new Intent();
        setResult(RESULT_CANCELED, i);
        finish();
    }
}