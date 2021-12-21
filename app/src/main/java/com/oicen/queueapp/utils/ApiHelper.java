package com.oicen.queueapp.utils;

import static com.oicen.queueapp.BuildConfig.HOST;

public class ApiHelper {
    public static String HOST_URL = HOST;
    public static String SIGNUP_URL = HOST + "register.php?apicall=signup";
    public static String IMAGE_PATH =  HOST + "images/";
    public static String CAT_PATH =  HOST + "images/categories/";
    public static String ITEM_URL = HOST_URL + "getItems.php";
    public static String ITEM_URLCAT = HOST + "getItemCategory.php?id=";
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
}


