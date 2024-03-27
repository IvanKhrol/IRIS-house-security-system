package com.example.irishousesecuritysystem;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Settings extends AppCompatActivity {
    public ImageButton  button_back;
    public Button       button_about, button_manual, button_contact;
    public TextView     textView_info;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        button_back     = findViewById(R.id.button_back);
        button_about    = findViewById(R.id.button_about);
        button_manual   = findViewById(R.id.button_manual);
        button_contact  = findViewById(R.id.button_contact);
        textView_info   = findViewById(R.id.textView_info);


        button_back.setOnClickListener(this::onClickBack);
        button_about.setOnClickListener(this::onClickAbout);
        button_manual.setOnClickListener(this::onClickManual);
        button_contact.setOnClickListener(this::onClickContact);

    }

    //==================================================================================================================================
    //                                                              onClick
    //==================================================================================================================================
    public void onClickBack(View v) {
        Intent intent = getIntent();
        intent.putExtra("key", "Test Return");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickAbout(View v) {
        String info_str = "";

        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
            info_str += "Application version: " + pInfo.versionName + "\n";
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("MyLog", "Exception in onClickAbout", e);
            throw new RuntimeException(e);
        }

        info_str += "Device: "        + Build.DEVICE                     + "\n";
        info_str += "Model: "         + Build.MODEL                      + "\n";
        info_str += "Product: "       + Build.PRODUCT                    + "\n";
        textView_info.setText(info_str);

    }
    public void onClickManual(View v) {
        textView_info.setText(R.string.manyal);
    }
    public void onClickContact(View v) {
        textView_info.setText(R.string.contact);
    }
}