package com.oicen.queueapp.utils;

import static android.content.Context.WIFI_SERVICE;
import static com.oicen.queueapp.BuildConfig.HOST;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

public class ApiHelper {
    public static String SIGNUP_URL = "register.php?apicall=signup";
    public static String IMAGE_PATH =  "images/";
    public static String CAT_PATH =  "images/categories/";
    public static String ITEM_URL = "getItems.php";
    public static String ITEM_URLCAT = "getItemCategory.php?id=";
    public static String FAVE = "setFavorite.php";
    public static String QUERY_URL = "getItems.php";
    public static String CATEGORY_URL = "getCategories.php";
    public static String INSERT_URL = "addOrder.php";
    public static String LOGIN_URL = "register.php?apicall=login";
    public static String FETCH_URL = "getQueue.php";
    public static String REMOVE = "setOrderDone.php";
    public static String GET_TIME = "getTime.php";
    public static String GET_USER = "getUser.php";
    public static String INIT = "test.php";
    public static String GET_ORDERS = "getOrders.php";
    public static String UPDATE_USER = "updateUser.php";
    public static String RECENT_ORDERS = "getRecentOrders.php";
//    Context context;
//
//    public ApiHelper(Context context){
//        this.context = context;
//    }

//    public String getHotspotAddress(){
//        final WifiManager manager = (WifiManager) context.getSystemService(WIFI_SERVICE);
//        final DhcpInfo dhcp = manager.getDhcpInfo();
//        int ipAddress = dhcp.gateway;
//        ipAddress = (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) ?
//                Integer.reverseBytes(ipAddress) : ipAddress;
//        byte[] ipAddressByte = BigInteger.valueOf(ipAddress).toByteArray();
//        try {
//            InetAddress myAddr = InetAddress.getByAddress(ipAddressByte);
//            return "http://" + myAddr.getHostAddress() + "/API/";
//        } catch (UnknownHostException e) {
//            // TODO Auto-generated catch block
//            Log.e("Wifi Class", "Error getting Hotspot IP address ", e);
//        }
//        return "null";
//    }
}


