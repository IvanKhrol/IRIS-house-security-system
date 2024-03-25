package com.example.irishousesecuritysystem.utils;

import android.util.Log;
public class NetworkUtils {

    //==================================================================================================================================
    //                                                              Private fields
    //==================================================================================================================================
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather?";
    private String city_name, API_key;
    public String url_str;

    //==================================================================================================================================
    //                                                              Constructors
    //==================================================================================================================================

    public NetworkUtils() {
        this.city_name = "";
        this.API_key = "";
    }

    public NetworkUtils(String city_name, String API_key) {
        this();
        this.city_name = city_name;
        this.API_key = API_key;
    }

    //==================================================================================================================================
    //                                                              SETS
    //==================================================================================================================================
    public void set_city_name(String new_city_name) {
        city_name = new_city_name;
    }

    public void set_API_key(String new_API_key) {
        API_key = new_API_key;
    }


    //==================================================================================================================================
    //                                                              generateURL
    //==================================================================================================================================

    public void generateURL() {
        if (city_name.isEmpty() || API_key.isEmpty()) {
            Log.d("MyLog", "Exception in generateURL: null city_name or API_key!");
            throw new RuntimeException();
        }

         url_str = BASE_URL +
                "q=" + city_name +
                "&appid=" + API_key +
                "&units=metric";
    }

}