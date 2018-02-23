package com.haocent.android.app_location;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * 定位
 *
 * Created by Tnno Wu on 2018/02/08.
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static String[] REQUEST_PERMISSION = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, REQUEST_PERMISSION, 666);

        Button btnGoogleMapLocation = findViewById(R.id.btn_google_map_location);
        Button btnCustomLocation = findViewById(R.id.btn_custom_location);

        btnGoogleMapLocation.setOnClickListener(this);
        btnCustomLocation.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_google_map_location:
                Intent intentGoogleMapLocation = new Intent(this, GoogleMapLocationActivity.class);
                startActivity(intentGoogleMapLocation);
                break;
            case R.id.btn_custom_location:
                Intent intentCustomLocation = new Intent(this, CustomLocationActivity.class);
                startActivity(intentCustomLocation);
                break;
            default:
                break;
        }
    }
}
