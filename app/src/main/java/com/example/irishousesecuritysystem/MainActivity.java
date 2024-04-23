package com.example.irishousesecuritysystem;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import androidx.core.content.ContextCompat;


import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.irishousesecuritysystem.utils.MyForegroundService;
import com.example.irishousesecuritysystem.utils.NetworkUtils;


public class MainActivity extends AppCompatActivity {

    //==================================================================================================================================
    //                                                              Fields
    //==================================================================================================================================
    public Button       button_connection, button_settings, button_home, button_profile;
    public TextView     textView_result;
    public RequestQueue queue;
    public ActivityResultLauncher<Intent> launcher = null;
    public NetworkUtils house_security_api;
    public boolean isServiceRun = false;
    final static String KEY_SAVE_TEXT_EDIT_RESULT = "SAVE_TEXT_EDIT_RESULT";
    final static String KEY_SAVE_BUTTON_CONNECTION = "SAVE_BUTTON_CONNECTION";
    final static String KEY_SAVE_IS_SERVICE_RUN = "IS_SERVICE_RUN";
    final static String KEY_DETECTION = "KEY_DETECTION";
    final static String KEY_ERROR = "KEY_ERROR";
    public String GET = "get_last_detection";
    private SharedPreferences sharedPref = null;
    //==================================================================================================================================
    //                                                              onCreate
    //==================================================================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPref = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);

        button_connection   = findViewById(R.id.button_connection);
        button_settings     = findViewById(R.id.button_settings);
        button_home         = findViewById(R.id.button_home);
        button_profile      = findViewById(R.id.button_profile);

        textView_result     = findViewById(R.id.textView_result);

        house_security_api = new NetworkUtils(GET);

        queue = Volley.newRequestQueue(this);

        onRestoreInstanceState(savedInstanceState);

        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                assert result.getData() != null;
                String test = result.getData().getStringExtra("key");
                Log.d("MyLog", "registerForActivityResult "  + test);
            }
        });

        button_connection.setOnClickListener(v -> {
        Intent serviceIntent = new Intent(this, MyForegroundService.class);
            if(isServiceRun) {
                stopService(serviceIntent);
                isServiceRun = false;
                button_connection.setText(R.string.button_connect_text);
            } else {
                ContextCompat.startForegroundService(this, serviceIntent);
                isServiceRun = true;
                button_connection.setText(R.string.button_disconnect_text);
            }
        });

        button_settings.setOnClickListener(this::onClickGoSettings);
        button_home.setOnClickListener      (v -> Toast.makeText(MainActivity.this, "Now it does not working", Toast.LENGTH_SHORT).show());

        button_profile.setOnClickListener   (v -> Toast.makeText(MainActivity.this, "Now it does not working", Toast.LENGTH_SHORT).show());
    }

    //==================================================================================================================================
    //                                                        save and load Activity
    //==================================================================================================================================
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        String tmp_str_textView_result   = textView_result.getText().toString();
        String tmp_str_button_connection = button_connection.getText().toString();


        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(KEY_SAVE_TEXT_EDIT_RESULT,  tmp_str_textView_result);
        editor.putString(KEY_SAVE_BUTTON_CONNECTION, tmp_str_button_connection);
        editor.putBoolean(KEY_SAVE_IS_SERVICE_RUN,   isServiceRun);
        editor.apply();

        Log.d("MyLog", "onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if(savedInstanceState != null) {
            super.onRestoreInstanceState(savedInstanceState);
        }

        String tmp_str_textView_result   = sharedPref.getString(KEY_SAVE_TEXT_EDIT_RESULT,  "");
        String tmp_str_button_connection = sharedPref.getString(KEY_SAVE_BUTTON_CONNECTION, getString(R.string.button_connect_text));
        isServiceRun = sharedPref.getBoolean(KEY_SAVE_IS_SERVICE_RUN, false);

        Log.d("MyLog", "onRestoreInstanceState:  " + tmp_str_button_connection +
                                " isServiceRun: " + isServiceRun);

        textView_result.setText(tmp_str_textView_result);
        button_connection.setText(tmp_str_button_connection);
    }
    //==================================================================================================================================
    //                                                    onClickGoSettings
    //==================================================================================================================================

    public void onClickGoSettings(View view) {
        launcher.launch(new Intent(this, Settings.class));
    }

    //==================================================================================================================================
    //                                                            getDataFromService
    //==================================================================================================================================
    private final BroadcastReceiver uiUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String detection_str = sharedPref.getString(KEY_DETECTION, "");
            String error;
            Log.d("MyLog", "onReceive " + detection_str);
            switch (detection_str) {
                case "false":
                    textView_result.setText(R.string.all_good);
                    break;
                case "true":
                    textView_result.setText(R.string.attention);
                    break;
                case "JSON ERROR":
                    error = sharedPref.getString(KEY_ERROR, "");
                    Toast.makeText(MainActivity.this, "Failed to get JSON:" +
                             error, Toast.LENGTH_LONG).show();
                    break;
                case "VOLLEY ERROR":
                    error = sharedPref.getString(KEY_ERROR, "");
                    Toast.makeText(MainActivity.this, "Failed to connect to the link with state:" +
                            error, Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    };

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(uiUpdateReceiver, new IntentFilter("updateUI"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(uiUpdateReceiver);
    }
}