package com.example.irishousesecuritysystem.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.irishousesecuritysystem.R;

import java.util.Timer;
import java.util.TimerTask;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;

public class MyForegroundService extends Service {

    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "ForegroundServiceChannel";
    private Timer timer;
    private TimerTask timerTask;
    public RequestQueue queue;
    private final String URL = "https://home-security-system-in-ru.onrender.com/get_last_detection";
    //    private  final  String URL =  "https://postman-echo.com/get";
    final static String KEY_DETECTION = "KEY_DETECTION";
    final static String KEY_ERROR = "KEY_ERROR";
    private SharedPreferences sharedPref = null;
    private static final int MY_SOCKET_TIMEOUT_MS = 600000;

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    public void onCreate() {
        super.onCreate();
        sharedPref = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        queue = Volley.newRequestQueue(this);
        registerReceiver(sendNotificationBroadcast, new IntentFilter("updateUI"));
        Log.d("MyLog_ForegroundService  ", "onCreate MyForegroundService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("MyLog_ForegroundService", "onStartCommand MyForegroundService");
        startForegroundService();
        startTimer();
        return START_STICKY;
    }

    @SuppressLint("ForegroundServiceType")
    private void startForegroundService() {
        // Create notification channel
        createNotificationChannel(CHANNEL_ID);

        // Create notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("My Foreground Service")         // TODO:   Change
                .setContentText("Service is running...")          // TODO:   Change
                .setSmallIcon(R.drawable.ic_launcher_foreground); // TODO:  Replace with your icon

        // Start foreground service
        startForeground(NOTIFICATION_ID, builder.build());
    }

    private void createNotificationChannel(String channel_id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    channel_id,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void startTimer() {
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                getURLData(URL);
                Log.d("MyLog_ForegroundService", "Timer task executed.");
            }
        };
        timer.schedule(timerTask, 0, 30000); // Execute task every 30 seconds
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public void onDestroy() {
        Log.d("MyLog_ForegroundService", "onDestroy MyForegroundService");
        super.onDestroy();
        stopTimer();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //==================================================================================================================================
    //                                                              getURLData
    //==================================================================================================================================
    private void getURLData(String url) {
        RequestQueue queue = Volley.newRequestQueue(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        Log.d("MyLog_ForegroundService", "getURLData");

        Intent intent = new Intent("updateUI");


        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                url, null, response -> {
            try {
                Log.d("MyLog_ForegroundService", "Response from " + url);
                boolean detection = response.getBoolean("detection");

                Log.d("MyLog_ForegroundService", "Response detection: " + detection);
                editor.putString(KEY_DETECTION, String.valueOf(detection));
                editor.apply();
                sendBroadcast(intent);
            } catch (JSONException e) {
                Log.d("MyLog_ForegroundService", "JSON error" + e);
                editor.putString(KEY_DETECTION, "JSON ERROR");
                editor.putString(KEY_ERROR, e.toString());
                editor.apply();
                sendBroadcast(intent);
                throw new RuntimeException(e);
            }
        }, volleyError -> {
            Log.d("MyLog_ForegroundService", "VolleyError: " + volleyError);
            editor.putString(KEY_DETECTION, "VOLLEY ERROR");
            if (volleyError.networkResponse != null) {
                editor.putString(KEY_ERROR, String.valueOf(volleyError.networkResponse.statusCode));
            } else {
                editor.putString(KEY_ERROR, "Other Server Error");
            }
            sendBroadcast(intent);
            editor.apply();
        });
        request.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                2,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }

    //==================================================================================================================================
    //                                                            sendNotification
    //==================================================================================================================================
    private final BroadcastReceiver sendNotificationBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String detection_str = sharedPref.getString(KEY_DETECTION, "");
            String error;
            Log.d("MyLog_ForegroundService", "onReceive " + detection_str);
            switch (detection_str) {
                case "true":
                    sendNotification(getString(R.string.attention));
                    break;
                case "JSON ERROR":
                case "VOLLEY ERROR":
                    error = sharedPref.getString(KEY_ERROR, "");
                    sendNotification(error);
                    break;
                default:
                    break;
            }
        }
    };

    private void sendNotification(String str) {
        createNotificationChannel(CHANNEL_ID);

        // Create notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)  // TODO: Set the notification icon
                .setContentTitle("Server Notification")         // TODO: Set the title
                .setContentText(str)  // Set the text
                .setPriority(NotificationCompat.PRIORITY_DEFAULT); // TODO: Set the priority

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(2, builder.build());
    }
}