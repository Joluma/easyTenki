package com.joluma.easytenki;

import android.content.Context;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by lithium on 14/11/14.
 */
public class TimeFetch {
    private static final String GOOGLE_TIMEZONE_API = "https://maps.googleapis.com/maps/api/timezone/json?location=%s,%s&&timestamp=0&key=%s";

    public static JSONObject getJSON(Context context, String lat, String lon){
        try {
            URL url = new URL(String.format(GOOGLE_TIMEZONE_API, lat, lon, "AIzaSyB4gWoi6XrisHJ5Lt8HuvUmcS44vw9fXZQ"));
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuffer json = new StringBuffer(1024);
            String tmp="";
            while((tmp=reader.readLine())!=null) {
                json.append(tmp).append("\n");
            }
            reader.close();

            JSONObject data = new JSONObject(json.toString());

            // This value will be 404 if the request was not successful
            if(!data.getString("status").equals("OK")){
                return null;
            }

            return data;
        }catch(Exception e){
            return null;
        }
    }
}
