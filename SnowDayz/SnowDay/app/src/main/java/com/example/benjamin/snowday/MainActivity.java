package com.example.benjamin.snowday;

import org.json.JSONException;

import com.example.benjamin.snowday.model.Weather;
import com.example.benjamin.snowday.adapter.DailyForecastPageAdapter;
//import com.example.benjamin.snowday.model.Location;
import com.example.benjamin.snowday.model.Weather;
import com.example.benjamin.snowday.model.WeatherForecast;

import android.widget.*;
import android.view.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Menu;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.widget.ImageView;
import android.widget.TextView;

import android.location.Location;
import android.location.Geocoder;
import android.location.LocationListener;
import android.location.LocationManager;
import android.content.Context;
import android.location.Criteria;

import java.util.List;
import java.util.Locale;

public class MainActivity extends FragmentActivity {


    private TextView cityText;
    private TextView condDescr;
    private TextView temp;
    //private TextView press;
    //private TextView windSpeed;
    //private TextView windDeg;
    private TextView unitTemp;

    //private TextView hum;
    private ImageView imgView;
    private Button searchButton;
    private Button get_current_location;
    private EditText locationString;

    private static String forecastDaysNum = "3";
    private ViewPager pager;
    private Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchButton = (Button) findViewById(R.id.go_button);
        locationString = (EditText) findViewById(R.id.location_entry);
        searchButton.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        String city = locationString.getText().toString();


                        setContentView(R.layout.output);
                        cityText = (TextView) findViewById(R.id.cityText);
                        condDescr = (TextView) findViewById(R.id.skydesc);
                        temp = (TextView) findViewById(R.id.temp);
                        unitTemp = (TextView) findViewById(R.id.unittemp);
                        unitTemp.setText("°C");

                        //hum = (TextView) findViewById(R.id.hum);
                        //press = (TextView) findViewById(R.id.press);
                        //windSpeed = (TextView) findViewById(R.id.windSpeed);
                        //windDeg = (TextView) findViewById(R.id.windDeg);
                        imgView = (ImageView) findViewById(R.id.condIcon);

                        JSONWeatherTask task = new JSONWeatherTask();
                        task.execute(new String[]{city});

                    }
                });

        /*get_current_location = (Button) findViewById(R.id.get_current_location);
        // getLocationButton is the name of your button.  Not the best name, I know.
        get_current_location.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // instantiate the location manager, note you will need to request permissions in your manifest
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                // get the last know location from your location manager.
                Location location= locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                // now get the lat/lon from the location and do something with it.
                JSONWeatherTask task = new JSONWeatherTask();
                Geocoder gcd = new Geocoder(context, Locale.getDefault());
                List<Address> addresses = gcd.getFromLocation(lat, lng, 1);
                addresses.get(0).getLocality());
                task.execute(location.getLatitude(), location.getLongitude());
            }
        });*/

        /*setContentView(R.layout.output);

        cityText = (TextView) findViewById(R.id.cityText);
        condDescr = (TextView) findViewById(R.id.condDesc);
        temp = (TextView) findViewById(R.id.temp);
        hum = (TextView) findViewById(R.id.hum);
        press = (TextView) findViewById(R.id.press);
        windSpeed = (TextView) findViewById(R.id.windSpeed);
        windDeg = (TextView) findViewById(R.id.windDeg);
        imgView = (ImageView) findViewById(R.id.imgView);

        //JSONWeatherTask task = new JSONWeatherTask();
        //task.execute(new String[]{city});*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    private class JSONWeatherTask extends AsyncTask<String, Void, Weather> {

        @Override
        protected Weather doInBackground(String... params) {
            //Weather weather = new Weather();
            String data = ((new WeatherHttpClient()).getWeatherData(params[0]));

            //try {

            Weather weather = new Weather();
            try {
                weather = JSONWeatherParser.getWeatherData(data);
            } catch (JSONException e) {
                e.printStackTrace();
            }


            // Let's retrieve the icon
            weather.iconData = ((new WeatherHttpClient()).getImage(weather.currentCondition.getIcon()));

            //} catch (JSONException e) {
            //e.printStackTrace();
            //}
            return weather;

        }


        @Override
        protected void onPostExecute(Weather weather) {
            super.onPostExecute(weather);

            if (weather.iconData != null && weather.iconData.length > 0) {
                Bitmap img = BitmapFactory.decodeByteArray(weather.iconData, 0, weather.iconData.length);
                imgView.setImageBitmap(img);
            }

            cityText.setText(weather.location.getCity() + "," + weather.location.getCountry());
            condDescr.setText(weather.currentCondition.getCondition() + "(" + weather.currentCondition.getDescr() + ")");
            temp.setText("" + Math.round((weather.temperature.getTemp() - 273.15)) + "°C");
            //hum.setText("" + weather.currentCondition.getHumidity() + "%");
            //press.setText("" + weather.currentCondition.getPressure() + " hPa");
            //windSpeed.setText("" + weather.wind.getSpeed() + " mps");
            //windDeg.setText("" + weather.wind.getDeg() + "°");

        }

        private class JSONForecastWeatherTask extends AsyncTask<String, Void, WeatherForecast> {

            @Override
            protected WeatherForecast doInBackground(String... params) {

                String data = ((new WeatherHttpClient()).getForecastWeatherData(params[0], params[1], params[2]));
                WeatherForecast forecast = new WeatherForecast();
                try {
                    forecast = JSONWeatherParser.getForecastWeather(data);
                    System.out.println("Weather [" + forecast + "]");
                    // Let's retrieve the icon
                    Weather weather = new Weather();
                    weather.iconData = ( (new WeatherHttpClient()).getImage(weather.currentCondition.getIcon()));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return forecast;

            }


            @Override
            protected void onPostExecute(WeatherForecast forecastWeather) {
                super.onPostExecute(forecastWeather);

                DailyForecastPageAdapter adapter = new DailyForecastPageAdapter(Integer.parseInt(forecastDaysNum), getSupportFragmentManager(), forecastWeather);

                pager.setAdapter(adapter);
            }


        }
    }
}