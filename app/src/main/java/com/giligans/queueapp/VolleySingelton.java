package com.giligans.queueapp;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class VolleySingelton {
    private static VolleySingelton mInstance;
    private RequestQueue mRequestQueue;
    private static Context mCtx;

    public VolleySingelton(Context conetxt) {
        mCtx = conetxt;
        mRequestQueue = getRequestQueu();
    }

    public static synchronized VolleySingelton getInstance(Context context){
        if(mInstance == null){
            mInstance = new VolleySingelton(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueu(){
        if(mRequestQueue == null){
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req){
        getRequestQueu().add(req);
    }
}