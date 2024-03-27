package com.example.irishousesecuritysystem;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.irishousesecuritysystem.utils.NetworkUtils;

import org.json.JSONException;
import org.json.JSONObject;



public class MainActivity extends AppCompatActivity {

    //==================================================================================================================================
    //                                                              Private fields
    //==================================================================================================================================
    public Button      button_connection, button_settings, button_home, button_profile;
    public TextView    textView_result;
    public RequestQueue queue;
    public ActivityResultLauncher<Intent> launcher = null;

    final static String KEY_SAVE_TEXT_EDIT_RESULT = "SAVE_TEXT_EDIT_RESULT";
    public String CITY_NAME = "Moscow", APi_KEY = "f8d1450d706f5ad5e5650236ed7d3bdf"; //TODO: delete it is test


    //==================================================================================================================================
    //                                                              onCreate
    //==================================================================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button_connection   = findViewById(R.id.button_connection);
        button_settings     = findViewById(R.id.button_settings);
        button_home         = findViewById(R.id.button_home);
        button_profile      = findViewById(R.id.button_profile);

        textView_result     = findViewById(R.id.textView_result);

        NetworkUtils weather_api = new NetworkUtils(CITY_NAME, APi_KEY);
        weather_api.generateURL();

        queue = Volley.newRequestQueue(this);

        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                assert result.getData() != null;
                String test = result.getData().getStringExtra("key");
                Log.d("MyLog", "registerForActivityResult "  + test);
            }
        });

        button_connection.setOnClickListener(v -> getURLData(weather_api.url_str));

        button_settings.setOnClickListener(this::onClickGoSettings);
        button_home.setOnClickListener      (v -> Toast.makeText(MainActivity.this, "Now it does not working", Toast.LENGTH_SHORT).show());

        button_profile.setOnClickListener   (v -> Toast.makeText(MainActivity.this, "Now it does not working", Toast.LENGTH_SHORT).show());
    }

    //==================================================================================================================================
    //                                                        save and load Activity
    //==================================================================================================================================
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        String tmp_str = textView_result.getText().toString();
        outState.putString(KEY_SAVE_TEXT_EDIT_RESULT, tmp_str);
        Log.d("MyLog", "onSaveInstanceState:  " + tmp_str);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String tmp_str = savedInstanceState.getString(KEY_SAVE_TEXT_EDIT_RESULT);
        Log.d("MyLog", "onRestoreInstanceState:  " + tmp_str);
        textView_result.setText(tmp_str);
    }
    //==================================================================================================================================
    //                                                    onClickGoSettings
    //==================================================================================================================================

    public void onClickGoSettings(View view) {
        launcher.launch(new Intent(this, Settings.class));
    }

    //==================================================================================================================================
    //                                                              getURLData
    //==================================================================================================================================
    private void getURLData(String url) {
        RequestQueue queue = Volley.newRequestQueue(this);
//        String url_tmp = "https://asss.com";
        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                url, null, response -> {
            try {
                Log.d("MyLog", "Response from " + url);
                JSONObject weather = response.getJSONObject("main");  //получаем JSON-обьекты main(в фигурных скобках - объекты, в квадратных - массивы (JSONArray)).
                Double temp = weather.getDouble("temp");
                Log.d("MyLog", "Response temp " + temp);
                setValues(temp);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }, volleyError -> {
            Log.d("MyLog", "VolleyError: " + url, volleyError);
            Toast.makeText(MainActivity.this, "Failed to connect to the link:" + url, Toast.LENGTH_LONG).show();
            throw new RuntimeException(volleyError);
        });

        queue.add(request);
    }

    //==================================================================================================================================
    //                                                              setValues
    //==================================================================================================================================

    @SuppressLint("SetTextI18n")
    public void setValues(Double val) {
        textView_result.setText("Temperature in " + CITY_NAME + ": " + val.toString() + "°C");
    }
}