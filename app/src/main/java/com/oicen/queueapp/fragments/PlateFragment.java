package com.oicen.queueapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.oicen.queueapp.R;
import com.oicen.queueapp.activities.MainApp;
import com.oicen.queueapp.adapters.PlateItemAdapter;
import com.oicen.queueapp.interfaces.TotalChangedListener;
import com.oicen.queueapp.models.PlateModel;
import com.oicen.queueapp.network.QueueListener;
import com.oicen.queueapp.utils.ApiHelper;
import com.oicen.queueapp.utils.VolleySingleton;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PlateFragment extends Fragment {
    final String INSERT_URL = ApiHelper.INSERT_URL;
    RecyclerView plateListRecycler;
    public PlateItemAdapter plateItemAdapter;
    ArrayList<PlateModel> plateModel;
    MaterialButton placeOrder;
    public TextView total;
    int totalAmount;
    Context context;
    FragmentManager fragmentManager;
    public View view;
    ArrayList<PlateModel> orderlist;
    int time;
    PlateFragment plateFragment;
    RadioButton radbtn;
    int usedTables[];

    public PlateFragment() { }

    private TotalChangedListener totalChangedListener = new TotalChangedListener() {
        @Override
        public void onItemClick(String text) {
            total.setText(text);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        plateFragment = this;
        context = getActivity();
        view = inflater.inflate(R.layout.fragment_plate, container, false);
        totalAmount = 0;
        plateModel = new ArrayList<PlateModel>();
        orderlist = new ArrayList<PlateModel>();
        placeOrder = (MaterialButton) view.findViewById(R.id.placeOrder);
        plateListRecycler = (RecyclerView) view.findViewById(R.id.plateRecyclerView);
        total = (TextView) view.findViewById(R.id.total);

        fetchPlateItems();

        if(totalAmount > 0){
            if(((MainApp)getActivity()).connectivity) {
                placeOrder.setEnabled(true);
            }
            total.setText("₱ " + String.format("%,d", totalAmount));
        }

        placeOrder.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) { 
                    final AlertDialog.Builder alert = new AlertDialog.Builder(context);
                    View mView = getLayoutInflater().inflate(R.layout.placeorder_dialog, null);
                    TextView message = (TextView) mView.findViewById(R.id.message);
                    EditText note = (EditText) mView.findViewById(R.id.editNote);
                    MaterialButton btn_cancel = (MaterialButton) mView.findViewById(R.id.btn_cancel);
                    MaterialButton btn_okay = (MaterialButton) mView.findViewById(R.id.btn_okay);
                    CheckBox senior = (CheckBox) mView.findViewById(R.id.senior);
                    RadioGroup radioGroup = (RadioGroup) mView.findViewById(R.id.radioGroup);
                    message.setText("Are you sure you want to place your order with the total of " + total.getText() + " ?");
                    alert.setView(mView);

                    final AlertDialog alertDialog = alert.create();
                    alertDialog.setCanceledOnTouchOutside(false);
                    btn_cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                        }
                    });
                    btn_okay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int type = radioGroup.getCheckedRadioButtonId();
                            RadioButton radioType = (RadioButton) mView.findViewById(type);
                            insertToDB(note.getText().toString(), radioType.getText().toString().equals("DINE IN") ? 1 : 0, senior.isChecked() ? 1 : 0);

                            total.setText("");

                            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                            ft.replace(R.id.fragment_container, new QueueFragment());
                            ft.addToBackStack(null);
                            ft.commit();
                            totalAmount = 0;
                            ((MainApp) getActivity()).bottomNav.setSelectedItemId(R.id.navigation_line);
                            ((MainApp) getActivity()).inqueue = true;

                            final Handler handler = new Handler(Looper.getMainLooper());
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent serviceIntent = new Intent(context, QueueListener.class);
                                    serviceIntent.putExtra("inputExtra", "Test");
                                    ContextCompat.startForegroundService(context, serviceIntent);
                                }
                            }, 1000);
                            alertDialog.dismiss();
                        }
                    });
                    alertDialog.show();

            }
        });

        return view;
    }

    public void fetchPlateItems(){
        SharedPreferences sp = getActivity().getSharedPreferences("plate_list", Context.MODE_PRIVATE);
        String json = sp.getString("orderlist", null);
        if(json != null){
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<PlateModel>>() {}.getType();
            ArrayList<PlateModel> newList = new ArrayList<PlateModel>();
            newList = gson.fromJson(json, type);
            orderlist = gson.fromJson(json, type);
            for(int i = 0; i < newList.size(); i++){
                totalAmount += (newList.get(i).getTotal());
                plateModel.add(new PlateModel(newList.get(i).getName(), newList.get(i).getPrice(), newList.get(i).getQty(), newList.get(i).getTotal(), newList.get(i).getBigimageurl()));
            }
        }

        setPlateListRecycler(plateModel);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        placeOrder.setEnabled(totalAmount > 0);
        fragmentManager = getActivity().getSupportFragmentManager();
    }

    void insertToDB(String note, int type, int isSenior){
        int amount = totalAmount;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, INSERT_URL,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (!obj.getBoolean("error")) {
                            SharedPreferences sp = getActivity().getSharedPreferences("plate_list", Context.MODE_PRIVATE);
                            sp.edit().clear().commit();
                            Toast.makeText(context, obj.getString("message"), Toast.LENGTH_LONG).show();

                            plateModel = new ArrayList<PlateModel>();
                            setPlateListRecycler(plateModel);
                            ((MainApp)getActivity()).setBadgeCount(0);

                        } else {
                            ((MainApp) context).showMessage(obj.getString("message"));

                        }
                    }catch (Exception e){
                        ((MainApp) context).showMessage(e.getMessage());
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    ((MainApp) context).showMessage(volleyError.toString() + " order");
                }
            }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                SharedPreferences sharedPreferences = getContext().getSharedPreferences("login", Context.MODE_PRIVATE);
                String name = sharedPreferences.getString("keyfname", null) + " " + sharedPreferences.getString("keylname", null);
                String data = new Gson().toJson(orderlist);
                String customer_id = sharedPreferences.getString("keyid", null);
                int uuid = Integer.parseInt(customer_id) + 1000;
                String queue_id = String.valueOf(uuid);

                ((MainApp)getActivity()).queueid = queue_id;

                params.put("queue_id", queue_id);
                params.put("customer_id", customer_id);
                params.put("customer_name", name);
                params.put("orderlist", data);
                params.put("type", String.valueOf(type));
                params.put("senior", String.valueOf(isSenior));
                params.put("note", note);

                return params;
            }
        };
        VolleySingleton.getInstance(context).addToRequestQueue(stringRequest);

    }

    public void setPlateListRecycler(ArrayList<PlateModel> plateModel) {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 1, GridLayoutManager.VERTICAL, false);
        plateListRecycler.setLayoutManager(gridLayoutManager);
        plateItemAdapter = new PlateItemAdapter(context, totalChangedListener, plateModel);
        plateListRecycler.setAdapter(plateItemAdapter);

        ItemTouchHelper.SimpleCallback touchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            private Drawable deleteIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_delete_24dp);
            private final ColorDrawable background = new ColorDrawable(Color.parseColor("#80f70d1a"));

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                final PlateModel entity = plateItemAdapter.getEntity(viewHolder.getAdapterPosition());
                ((MainApp)context).dereaseBadgeCount();
                int temptotal = entity.getTotal();
                totalAmount -= temptotal;
                plateItemAdapter.removeItem(viewHolder.getAdapterPosition());
                if(((MainApp)context).getItemCount() <= 0){
                    placeOrder.setEnabled(false);
                }

                int sum = 0;
                for(PlateModel p : plateModel){
                    sum += p.getTotal();
                }

                total.setText("TOTAL : ₱ " + String.format("%,d", sum));

                Snackbar snackbar = Snackbar.make(view, entity.getName()+" Removed", Snackbar.LENGTH_SHORT)
                    .setAction("UNDO", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ((MainApp)context).increaseBadgeCount();
                            placeOrder.setEnabled(true);
                            plateItemAdapter.undoDelete(entity, position);
                            totalAmount += temptotal;
                            int sum = 0;
                            for(PlateModel p : plateModel){
                                sum += p.getTotal();
                            }

                            total.setText("TOTAL : ₱ " + String.format("%,d", sum));

                        }
                    });
                snackbar.show();
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                View itemView = viewHolder.itemView;

                int iconMargin = (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
                int iconTop = itemView.getTop() + (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
                int iconBottom = iconTop + deleteIcon.getIntrinsicHeight();

                if (dX > 0) {
                    int iconLeft = itemView.getLeft() + iconMargin + deleteIcon.getIntrinsicWidth();
                    int iconRight = itemView.getLeft() + iconMargin;

                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                    background.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + ((int) dX), itemView.getBottom());
                } else if (dX < 0) {
                    int iconLeft = itemView.getRight() - iconMargin - deleteIcon.getIntrinsicWidth();
                    int iconRight = itemView.getRight() - iconMargin;

                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                    background.setBounds(itemView.getRight() + ((int) dX), itemView.getTop(), itemView.getRight(), itemView.getBottom());
                } else {
                    background.setBounds(0, 0, 0, 0);
                }

                background.draw(c);
                deleteIcon.draw(c);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(touchHelperCallback);
        itemTouchHelper.attachToRecyclerView(plateListRecycler);
    }
}