package com.joluma.easytenki;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;


public class TenkiActivity extends Activity {

    Typeface weatherFont;
    TextClock timeField;
    RelativeLayout mLayout;
    Button mChangeCityBtn;

    TextView cityField;
    TextView updatedField;
    TextView detailsField;
    TextView currentTemperatureField;
    TextView weatherIcon;

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        weatherFont = Typeface.createFromAsset(getAssets(), "fonts/tenki.ttf");
        timeField = (TextClock)findViewById(R.id.current_datetime_field);
        mLayout = (RelativeLayout)findViewById(R.id.relativeLayout);
        mChangeCityBtn = (Button)findViewById(R.id.action_change_city);

        cityField = (TextView)findViewById(R.id.city_field);
        updatedField = (TextView)findViewById(R.id.updated_field);
        detailsField = (TextView)findViewById(R.id.details_field);
        currentTemperatureField = (TextView)findViewById(R.id.current_temperature_field);
        weatherIcon = (TextView)findViewById(R.id.weather_icon);
        weatherIcon.setTypeface(weatherFont);

        mChangeCityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialog();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateWeatherData(new TenkiCity(this).getCity());
    }

    private void updateWeatherData(final String city){
        new Thread(){
            public void run(){
                final JSONObject json = TenkiFetch.getJSON(TenkiActivity.this, city);
                if(json == null){
                    handler.post(new Runnable(){
                        public void run(){
                            Toast.makeText(TenkiActivity.this, getString(R.string.place_not_found), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    handler.post(new Runnable(){
                        public void run(){
                            renderWeather(json);
                        }
                    });
                }
            }
        }.start();
    }

    private void renderWeather(JSONObject json){
        try {
            setTime(json.getJSONObject("coord").getString("lat"), json.getJSONObject("coord").getString("lon"));

            cityField.setText(json.getString("name").toUpperCase(Locale.US) +
                    ", " +
                    json.getJSONObject("sys").getString("country"));

            JSONObject details = json.getJSONArray("weather").getJSONObject(0);
            JSONObject main = json.getJSONObject("main");
            detailsField.setText(
                    details.getString("description").toUpperCase(Locale.US) +
                            "\n" + "Humidity: " + main.getString("humidity") + "%" +
                            "\n" + "Pressure: " + main.getString("pressure") + " hPa");

            currentTemperatureField.setText(
                    String.format("%.2f", main.getDouble("temp"))+ " ℃");

            DateFormat df = DateFormat.getDateTimeInstance();
            String updatedOn = df.format(new Date(json.getLong("dt")*1000));
            updatedField.setText("Last update: " + updatedOn);

            setWeatherIcon(details.getInt("id"),
                    json.getJSONObject("sys").getLong("sunrise") * 1000,
                    json.getJSONObject("sys").getLong("sunset") * 1000);
        }catch(Exception e){
            Log.e("SimpleWeather", "One or more fields not found in the JSON data");
        }
    }

    private void setWeatherIcon(int actualId, long sunrise, long sunset){
        int id = actualId / 100;
        String icon = "";
        if(actualId == 800){
            long currentTime = new Date().getTime();
            if(currentTime>=sunrise && currentTime<sunset) {
                icon = getString(R.string.weather_sunny);
            } else {
                icon = getString(R.string.weather_clear_night);
            }
        } else {
            switch(id) {
                case 2 : icon = getString(R.string.weather_thunder);
                    break;
                case 3 : icon = getString(R.string.weather_drizzle);
                    break;
                case 7 : icon = getString(R.string.weather_foggy);
                    break;
                case 8 : icon = getString(R.string.weather_cloudy);
                    break;
                case 6 : icon = getString(R.string.weather_snowy);
                    break;
                case 5 : icon = getString(R.string.weather_rainy);
                    break;
            }
        }
        weatherIcon.setText(icon);
    }

    private void showInputDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change city");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("Go", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                changeCity(input.getText().toString());
            }
        });
        builder.show();
    }

    private void setTime(final String lat, final String lon){
        new Thread(){
            public void run(){
                final JSONObject json = TimeFetch.getJSON(TenkiActivity.this, lat, lon);
                if(json == null){
                    handler.post(new Runnable(){
                        public void run(){
                            timeField.setVisibility(View.INVISIBLE);
                        }
                    });
                } else {
                    handler.post(new Runnable(){
                        public void run(){
                            try {
                                timeField.setTimeZone(json.getString("timeZoneId"));
                                timeField.setVisibility(View.VISIBLE);
                                changeBackground();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        }.start();
    }

    private void changeBackground() {
        int mHeure = Integer.parseInt(timeField.getText().toString().split(":")[0]);
        Drawable mBackgroundImage = null;

        if (mHeure >= 5 && mHeure < 11){
            mBackgroundImage = getResources().getDrawable(R.drawable.matin);
        } else if (mHeure >= 11 && mHeure < 17){
            mBackgroundImage = getResources().getDrawable(R.drawable.jour);
        } else if (mHeure >= 17 && mHeure < 20){
            mBackgroundImage = getResources().getDrawable(R.drawable.debut_soiree);
        } else if (mHeure >= 20 && mHeure < 24){
            mBackgroundImage = getResources().getDrawable(R.drawable.soiree);
        } else if (mHeure >= 0 && mHeure < 5){
            mBackgroundImage = getResources().getDrawable(R.drawable.nuit);
        }

        mLayout.setBackground(mBackgroundImage);
    }

    public void changeCity(String city){
        new TenkiCity(this).setCity(city);
        updateWeatherData(city);
    }
}
