package com.giligans.queueapp.fragments;

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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.giligans.queueapp.R;
import com.giligans.queueapp.activities.MainApp;
import com.giligans.queueapp.activities.Pay;
import com.giligans.queueapp.adapters.PlateItemAdapter;
import com.giligans.queueapp.interfaces.TotalChangedListener;
import com.giligans.queueapp.models.PlateModel;
import com.giligans.queueapp.network.QueueListener;
import com.giligans.queueapp.utils.RadioGridGroup;
import com.giligans.queueapp.utils.VolleySingleton;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.giligans.queueapp.BuildConfig.HOST;

public class PlateFragment extends Fragment {
    final String INSERT_URL = HOST + "insertorder.php";
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
    String customer_id;
    String queue_id;
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
                View mView = getLayoutInflater().inflate(R.layout.placeorder_dialog,null);
                final EditText inputText = (EditText)mView.findViewById(R.id.txt_input);
                TextView message = (TextView) mView.findViewById(R.id.message);
                MaterialButton btn_cancel = (MaterialButton)mView.findViewById(R.id.btn_cancel);
                MaterialButton btn_okay = (MaterialButton)mView.findViewById(R.id.btn_okay);
                CheckBox senior = (CheckBox) mView.findViewById(R.id.senior);
                RadioGroup radioGroup = (RadioGroup) mView.findViewById(R.id.radioGroup);
                RadioGridGroup table = (RadioGridGroup) mView.findViewById(R.id.radioGroups);
                LinearLayout tableLayout = (LinearLayout) mView.findViewById(R.id.table);
                message.setText("Are you sure you want to place your order with the total of " + total.getText() + " ?");
                alert.setView(mView);

                double totalCells = 17;
                double cols = 6;
                double calcRows = Math.ceil(totalCells / cols);

                int colNums = 1;
                for(int row = 1; row <= calcRows; row++)
                {
                    LinearLayout linear = new LinearLayout(context);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    linear.setLayoutParams(params);
                    linear.setOrientation(LinearLayout.HORIZONTAL);

                    for(int col = 1; col <= cols; col++) {
                        if(colNums > totalCells) break;
                        RadioButton rdbtn = new RadioButton(context);
                        rdbtn.setLayoutParams (new RadioGridGroup.LayoutParams(0, RadioGridGroup.LayoutParams.MATCH_PARENT, 1));
                        //if(col == 1 && row == 1) rdbtn.setChecked(true);
                        //rdbtn.setButtonTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                        rdbtn.setButtonDrawable(null);
                        rdbtn.setBackgroundResource(R.drawable.table_bg);
                        rdbtn.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                        rdbtn.setPadding(2,2,2,2);
                        rdbtn.setId(View.generateViewId());
                        rdbtn.setText(String.valueOf(colNums));
                        linear.addView(rdbtn);
                        colNums++;
                    }
                    ((ViewGroup) mView.findViewById(R.id.radioGroups)).addView(linear);
                }

//                int allTables[] = new int[17];
//                for(int i = 1; i <= 17; i++){ allTables[i-1] = i; }
//
//                usedTables = new int[4];
//                for(int i = 1; i <= 4; i++){ usedTables[i-1] = i; }
//
//                int[] availableTables = IntStream.concat(IntStream.of(allTables), IntStream.of(usedTables))
//                        .filter(x -> !IntStream.of(allTables).anyMatch(y -> y == x) || !IntStream.of(usedTables).anyMatch(z -> z == x))
//                        .toArray();
//
//                //Toast.makeText(context, Arrays.toString(availableTables), Toast.LENGTH_LONG).show();
//
//                int tableCol = 0;
//                for(int row = 0; row < calcRows; row++)
//                {
//                    LinearLayout linear = new LinearLayout(context);
//                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//                    linear.setLayoutParams(params);
//                    linear.setOrientation(LinearLayout.HORIZONTAL);
//
//                    for(int col = 0; col < cols; col++) {
//                        if(tableCol >= availableTables.length) break;
//                        RadioButton rdbtn = new RadioButton(context);
//                        rdbtn.setLayoutParams (new RadioGridGroup.LayoutParams(0, RadioGridGroup.LayoutParams.MATCH_PARENT, 1));
//                        if(col == 0 && row == 0) rdbtn.setChecked(true);
//                        rdbtn.setButtonTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
//                        rdbtn.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
//                        rdbtn.setPadding(2,2,2,2);
//                        rdbtn.setId(View.generateViewId());
//                        rdbtn.setText(String.valueOf(availableTables[tableCol]));
//                        linear.addView(rdbtn);
//                        tableCol++;
//
//                    }
//                    ((ViewGroup) mView.findViewById(R.id.radioGroups)).addView(linear);
//                }

                radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        RadioButton radbtn = (RadioButton) mView.findViewById(checkedId);
                        if(radbtn.getId() == R.id.radioButton2){
                            tableLayout.setVisibility(View.GONE);
                        }else{
                            tableLayout.setVisibility(View.VISIBLE);
                        }
                    }
                });

                //radbtn = (RadioButton) mView.findViewById();
                radbtn = new RadioButton(context);

                table.setOnCheckedChangeListener(new RadioGridGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGridGroup group, int checkedId) {
                        radbtn = (RadioButton) mView.findViewById(checkedId);
                    }
                });

                btn_okay.setEnabled(false);
                inputText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        try {
                            if (s.toString().length() > 0 && (Integer.parseInt(s.toString()) >= totalAmount)) {
                                btn_okay.setEnabled(true);
                            } else {
                                inputText.setError("Insufficient Amount !");
                                btn_okay.setEnabled(false);
                            }
                        } catch(NumberFormatException e) {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    }
                    @Override
                    public void afterTextChanged(Editable s) {}
                });
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
                        int tab = table.getCheckedRadioButtonId();
                        RadioButton tableNum = (RadioButton) mView.findViewById(tab);
                        String number = tableNum != null ? tableNum.getText().toString() : "0";

                        insertToDB(inputText.getText().toString(), radioType.getText().toString().equals("DINE IN") ? 1 : 0, senior.isChecked() ? 1 : 0, number);

                        total.setText("");

                        FragmentTransaction ft =  getActivity().getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.fragment_container, new LineFragment());
                        ft.addToBackStack(null);
                        ft.commit();
                        totalAmount = 0;
                        ((MainApp)getActivity()).bottomNav.setSelectedItemId(R.id.navigation_line);
                        ((MainApp)getActivity()).inqueue = true;

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
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("login", Context.MODE_PRIVATE);
        customer_id = !((MainApp)getActivity()).guest ? sharedPreferences.getString("keyid", null) : "0";

        int uuid = Integer.parseInt(customer_id) + (!((MainApp)getActivity()).guest ? 1000 : 2000);
        queue_id = String.valueOf(uuid);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        placeOrder.setEnabled(!((MainApp)getActivity()).inqueue && totalAmount > 0);
        fragmentManager = getActivity().getSupportFragmentManager();
    }

    void insertToDB(String cash, int type, int isSenior, String table){
        int amount = totalAmount;
        Toast.makeText(context, cash + " " + type + " " + isSenior + " " + table, Toast.LENGTH_LONG).show();

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

                            Intent payIntent = new Intent(context, Pay.class);
                            payIntent.putExtra("qr", queue_id + " " + customer_id);
                            startActivity(payIntent);

                        } else {
                            Toast.makeText(context, obj.getString("message"), Toast.LENGTH_LONG).show();

                        }
                    }catch (Exception e){
                        Toast.makeText(context, e.getMessage() + " here",  Toast.LENGTH_LONG).show();
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Toast.makeText(getContext(), volleyError.toString() + " order", Toast.LENGTH_LONG).show();
                }
            }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                SharedPreferences sharedPreferences = getContext().getSharedPreferences("login", Context.MODE_PRIVATE);
                String name = !((MainApp)getActivity()).guest ? sharedPreferences.getString("keyfname", null) + " " + sharedPreferences.getString("keylname", null) : "GUEST";
                String data = new Gson().toJson(orderlist);

                ((MainApp)getActivity()).queueid = queue_id;

                params.put("queue_id", queue_id);
                params.put("customer_id", customer_id);
                params.put("customer_name", name);
                params.put("orderlist", data);
                params.put("time", "1");
                params.put("cash", cash);
                params.put("type", String.valueOf(type));
                params.put("senior", String.valueOf(isSenior));
                params.put("table_number", table );

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

                           // totalChangedListener.onItemClick("₱ " + String.format("%,d", sum));
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