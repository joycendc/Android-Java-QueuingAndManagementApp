package com.oicen.queueapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;

import com.oicen.queueapp.R;

public class Pptc extends AppCompatActivity {
    WebView web;
    ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pptc);

        getSupportActionBar().hide();

        Bundle extras = getIntent().getExtras();
        String type = extras.getString("type");
        web = (WebView)findViewById(R.id.webView);

        if(type.equals("tc")){
            web.loadUrl("file:///android_asset/termsandcondition.html");
        }else {
            web.loadUrl("file:///android_asset/privacyandpolicy.html");
        }

        back = findViewById(R.id.back);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }
}