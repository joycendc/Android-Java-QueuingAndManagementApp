package com.oicen.queueapp.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.oicen.queueapp.R;
import com.oicen.queueapp.activities.MainApp;
import com.oicen.queueapp.models.OrderModel;
import com.oicen.queueapp.models.QueueModel;
import com.oicen.queueapp.network.QueueListener;
import com.oicen.queueapp.utils.ApiHelper;
import com.oicen.queueapp.utils.QueueDiffUtilCallBack;
import com.oicen.queueapp.utils.VolleySingleton;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.oicen.queueapp.BuildConfig.HOST;

public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.CustomerViewHolder> {
    final String FETCH_URL = HOST + ApiHelper.GET_ORDERS;
    final String CANCEL_URL = HOST + ApiHelper.REMOVE;
    public ArrayList<QueueModel> customer;
    Context context;
    RecyclerView ordersRecycler;
    OrderAdapter orderAdapter;
    ArrayList<OrderModel> orderList;
    AlertDialog.Builder resDialog;

    public QueueAdapter(Context context, ArrayList<QueueModel> customer) {
        this.context = context;
        this.customer = customer;

    }

    public void update(ArrayList<QueueModel> newData){
        QueueAdapter self = this;
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new QueueDiffUtilCallBack(newData, customer));
                customer.clear();
                diffResult.dispatchUpdatesTo(self);
                customer.addAll(newData);
                notifyDataSetChanged();
            }
        }, 500);

    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CustomerViewHolder(LayoutInflater.from(context).inflate(R.layout.queue_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, @SuppressLint("RecyclerView") int position) {

        resDialog =  new AlertDialog.Builder(context);
        SharedPreferences sharedPreferences = context.getSharedPreferences("login", Context.MODE_PRIVATE);
        String keyname = sharedPreferences.getString("keyfname", null) + " " + sharedPreferences.getString("keylname", null);
        holder.name.setText("CUSTOMER " + (position + 1));
        holder.id.setText(customer.get(position).getQueueId());

        if (customer.get(position).getStatus().equals("0")) {
            if (customer.get(position).getName().equals(keyname)) {
                holder.name.setText("You ( " + keyname.toUpperCase() + " )");
            }
            holder.card.setCardBackgroundColor(context.getResources().getColor(R.color.queueUnpaidOther));
            holder.name.setTextColor(context.getResources().getColor(R.color.queueTextDark));
            holder.id.setTextColor(context.getResources().getColor(R.color.queueTextDark));
            holder.status.setTextColor(context.getResources().getColor(R.color.queueTextDark));
            holder.image.setColorFilter(context.getResources().getColor(R.color.queueTextDark));
            if (customer.get(position).getName().equals(keyname)) {
                holder.card.setCardBackgroundColor(context.getResources().getColor(R.color.queueUnpaid));
            }
            holder.status.setText("UNPAID");

        } else {
            if (position == 0) {
                holder.card.setCardBackgroundColor(context.getResources().getColor(R.color.queueServingOther));
                if (customer.get(position).getName().equals(keyname)) {
                    holder.name.setText("You (" + keyname.toUpperCase() + ")");
                    holder.card.setCardBackgroundColor(context.getResources().getColor(R.color.queueServingSelf));
                    holder.name.setTextColor(context.getResources().getColor(R.color.queueTextLight));
                    holder.id.setTextColor(context.getResources().getColor(R.color.queueTextLight));
                    holder.status.setTextColor(context.getResources().getColor(R.color.queueTextLight));
                    holder.image.setColorFilter(context.getResources().getColor(R.color.queueTextLight));
                }
                holder.status.setText("NOW SERVING");
            } else {
                if (customer.get(position).getName().equals(keyname)) {
                    holder.name.setText("You ( " + keyname.toUpperCase() + " )");
                    holder.card.setCardBackgroundColor(context.getResources().getColor(R.color.queuePaid));
                    holder.name.setTextColor(context.getResources().getColor(R.color.queueTextLight));
                    holder.id.setTextColor(context.getResources().getColor(R.color.queueTextLight));
                    holder.status.setTextColor(context.getResources().getColor(R.color.queueTextLight));
                    holder.image.setColorFilter(context.getResources().getColor(R.color.queueTextLight));
                } else {
                    holder.card.setCardBackgroundColor(context.getResources().getColor(R.color.queuePaidOther));
                    holder.name.setTextColor(context.getResources().getColor(R.color.queueTextLight));
                    holder.id.setTextColor(context.getResources().getColor(R.color.queueTextLight));
                    holder.status.setTextColor(context.getResources().getColor(R.color.queueTextLight));
                    holder.image.setColorFilter(context.getResources().getColor(R.color.queueTextLight));
                }
                holder.status.setText("IN LINE (PAID)");
            }
        }

        fetchOrders();

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!((MainApp) context).isPaid) {
                    if (customer.get(position).getName().equals(keyname)) {
                        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
                        View mView = LayoutInflater.from(context).inflate(R.layout.order_details_dialog, null);
                        MaterialButton btn_cancel = (MaterialButton) mView.findViewById(R.id.btn_cancel);
                        MaterialButton btn_okay = (MaterialButton) mView.findViewById(R.id.btn_okay);
                        Button cancelOrder = (Button) mView.findViewById(R.id.cancel);
                        ordersRecycler = (RecyclerView) mView.findViewById(R.id.orderList);
                        TextView total = (TextView) mView.findViewById(R.id.total);

                        total.setVisibility(View.GONE);

                        alert.setView(mView);

                        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 1, GridLayoutManager.VERTICAL, false);
                        ordersRecycler.setLayoutManager(gridLayoutManager);
                        ordersRecycler.setItemAnimator(null);
                        orderAdapter = new OrderAdapter(context, orderList);
                        ordersRecycler.setAdapter(orderAdapter);

                        final AlertDialog alertDialog = alert.create();
                        //alertDialog.setCanceledOnTouchOutside(false);

                        btn_cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                alertDialog.dismiss();
                                ((MainApp) context).onBackPressed();
                            }
                        });
                        btn_okay.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                alertDialog.dismiss();
                            }
                        });
                        cancelOrder.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                resDialog
                                    .setTitle("Warning")
                                    .setMessage("Are you sure to cancel your order ?")
                                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent serviceIntent = new Intent(context, QueueListener.class);
                                            serviceIntent.putExtra("cancel", true);
                                            serviceIntent.setAction("STOP");
                                            ContextCompat.startForegroundService(context, serviceIntent);

                                            cancelOrder();
                                            
                                            dialog.dismiss();

                                        }
                                    })
                                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setCancelable(false)
                                    .show();
                            }
                        });
                        alertDialog.show();
                    }
                }
            }
        });
    }

    void cancelOrder(){
        final StringRequest stringRequest = new StringRequest(Request.Method.POST, CANCEL_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            String message = obj.getString("message");
                            if (!obj.getBoolean("error")) {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                            } else  {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                            }

                        } catch (Exception e) { e.printStackTrace(); }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                })  {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                SharedPreferences sharedPreferences = context.getSharedPreferences("login", Context.MODE_PRIVATE);
                String id = sharedPreferences.getString("keyid", null);
                params.put("customer_id", id);
                return params;
            }
            // @Override
            // public Map<String, String> getHeaders() throws AuthFailureError {
            //     Map<String, String> headers = new HashMap<>();
            //     headers.put(ApiHelper.KEY_COOKIE, ApiHelper.VALUE_CONTENT);
            //     return headers;
            // }
        };
        VolleySingleton.getInstance(context).addToRequestQueue(stringRequest);
    }

    void fetchOrders(){
        final StringRequest stringRequest = new StringRequest(Request.Method.POST, FETCH_URL,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    orderList = new ArrayList<OrderModel>();
                   
                    try {
                        JSONArray items = new JSONArray(response);
                        for (int i = 0; i < items.length(); i++) {
                            JSONObject itemObject = items.getJSONObject(i);

                            String name = itemObject.getString("item_name");
                            String qty = itemObject.getString("qty");
                            String price = itemObject.getString("price");
                            String total = itemObject.getString("total");

                            orderList.add(new OrderModel(name, qty, price, total));
                        }

                    } catch (JSONException e) { e.printStackTrace(); }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            })  {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                SharedPreferences sharedPreferences = context.getSharedPreferences("login", Context.MODE_PRIVATE);
                String id = sharedPreferences.getString("keyid", null);
                params.put("customer_id", id);
                return params;
            }
            // @Override
            // public Map<String, String> getHeaders() throws AuthFailureError {
            //     Map<String, String> headers = new HashMap<>();
            //     headers.put(ApiHelper.KEY_COOKIE, ApiHelper.VALUE_CONTENT);
            //     return headers;
            // }
        };
        VolleySingleton.getInstance(context).addToRequestQueue(stringRequest);
    }

    @Override
    public int getItemCount() {
        return customer.size();
    }

    public class CustomerViewHolder extends RecyclerView.ViewHolder {
        TextView name, id, status;
        CardView card;
        ImageView image;

        public CustomerViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.customerName);
            id = itemView.findViewById(R.id.customerNumber);
            status = itemView.findViewById(R.id.status);
            card = itemView.findViewById(R.id.row);
            image = itemView.findViewById(R.id.customerImage);
        }
    }
}