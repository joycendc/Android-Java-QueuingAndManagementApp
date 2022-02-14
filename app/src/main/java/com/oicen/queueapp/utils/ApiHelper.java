package com.oicen.queueapp.utils;

import static android.content.Context.WIFI_SERVICE;
import static com.oicen.queueapp.BuildConfig.HOST;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


public class ApiHelper {
    public static String SIGNUP_URL = "register.php?apicall=signup";
    public static String IMAGE_PATH = "images/";
    public static String CAT_PATH = "images/categories/";
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
    public static String KEY_COOKIE = "cookie";
    public static String VALUE_CONTENT = "__test=5083fb2d45d3ffeb754bd17e54732692; expires=Tue, 19 Jan 2038 03:14:07 UTC; PHPSESSID=bfa95c09254ed73071abd5eb05059595";
}