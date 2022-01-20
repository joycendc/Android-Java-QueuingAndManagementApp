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
    public static String HOST_URL = HOST;
    public static String SIGNUP_URL = HOST_URL + "register.php?apicall=signup";
    public static String IMAGE_PATH =  HOST_URL + "images/";
    public static String CAT_PATH =  HOST_URL + "images/categories/";
    public static String ITEM_URL = HOST_URL + "getItems.php";
    public static String ITEM_URLCAT = HOST_URL + "getItemCategory.php?id=";
    public static String FAVE = HOST_URL + "setFavorite.php";
    public static String QUERY_URL = HOST_URL + "getItems.php";
    public static String CATEGORY_URL = HOST_URL + "getCategories.php";
    public static String INSERT_URL = HOST_URL + "addOrder.php";
    public static String LOGIN_URL = HOST_URL + "register.php?apicall=login";
    public static String FETCH_URL = HOST_URL + "getQueue.php";
    public static String REMOVE = HOST_URL + "setOrderDone.php";
    public static String GET_TIME = HOST_URL + "getTime.php";
    public static String GET_USER = HOST_URL + "getUser.php";
    public static String INIT = HOST_URL + "test.php";
    public static String UPDATE_USER = HOST_URL + "updateUser.php";
    public static String RECENT_ORDERS = HOST_URL + "getRecentOrders.php";
    Context context;

    public ApiHelper(Context context){
        this.context = context;
    }

    public String getHotspotAddress(){
        final WifiManager manager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        final DhcpInfo dhcp = manager.getDhcpInfo();
        int ipAddress = dhcp.gateway;
        ipAddress = (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) ?
                Integer.reverseBytes(ipAddress) : ipAddress;
        byte[] ipAddressByte = BigInteger.valueOf(ipAddress).toByteArray();
        try {
            InetAddress myAddr = InetAddress.getByAddress(ipAddressByte);
            return myAddr.getHostAddress();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            Log.e("Wifi Class", "Error getting Hotspot IP address ", e);
        }
        return "null";
    }
}


