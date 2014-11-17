package com.joluma.easytenki;

import android.app.Activity;
import android.content.SharedPreferences;

/**
 * Created by lithium on 14/11/14.
 */
public class TenkiCity {

    SharedPreferences prefs;

    public TenkiCity(Activity activity){
        prefs = activity.getPreferences(Activity.MODE_PRIVATE);
    }

    // If the user has not chosen a city yet, return
    // Tokyo as the default city
    String getCity(){
        return prefs.getString("city", "Tokyo, JP");
    }

    void setCity(String city){
        prefs.edit().putString("city", city).commit();
    }
}
