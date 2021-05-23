package com.giligans.queueapp;

import android.content.Context;
import android.util.DisplayMetrics;

public class Utility {
    public int autoFitColumns(Context context, int columnWidthDp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns = (int) (screenWidthDp / columnWidthDp + 0.5); // +0.5 for correct rounding to int.
        int remaining = (int) screenWidthDp - (noOfColumns * columnWidthDp);
        if(remaining <= 30) noOfColumns = noOfColumns - 1;
        return noOfColumns > 4 ? noOfColumns - 1 : noOfColumns;
    }

    public  int calculateSpacing(Context context) {
        int columnWidthDp = 150;
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns = autoFitColumns(context, columnWidthDp);
        int remaining = (int) screenWidthDp - (noOfColumns * columnWidthDp);
        return remaining / (noOfColumns  + 1);
    }
}