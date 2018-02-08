package com.haocent.android.googlemap;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by Tnno Wu on 2018/02/08.
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnGoogleMapToolbar = findViewById(R.id.btn_google_map_toolbar);
        Button btnCustomToolbar = findViewById(R.id.btn_custom_toolbar);

        btnGoogleMapToolbar.setOnClickListener(this);
        btnCustomToolbar.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_google_map_toolbar:
                Intent intentGoogleMapToolbar = new Intent(this, GoogleMapToolbarActivity.class);
                startActivity(intentGoogleMapToolbar);
                break;
            case R.id.btn_custom_toolbar:
                Intent intentCustomToolbar = new Intent(this, CustomToolbarActivity.class);
                startActivity(intentCustomToolbar);
                break;
            default:
                break;
        }
    }
}
