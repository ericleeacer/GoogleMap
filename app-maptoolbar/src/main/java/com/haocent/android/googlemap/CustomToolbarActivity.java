package com.haocent.android.googlemap;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Tnno Wu on 2018/02/08.
 */

public class CustomToolbarActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "CustomToolbarActivity";

    private GoogleMap mMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_toolbar);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng dalian = new LatLng(38.9135883, 121.6148269);
        mMap.addMarker(new MarkerOptions().position(dalian).title("Marker in Dalian"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(dalian));
        mMap.getUiSettings().setMapToolbarEnabled(false);       // 隐藏 GoogleMap 默认的导航和 GPS 按钮

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuffer stringBuffer = new StringBuffer("google.navigation:q=")
                        .append(38.9135883)
                        .append(",")
                        .append(121.6148269)
                        .append("&mode=drive");
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(stringBuffer.toString()));
                intent.setPackage("com.google.android.apps.maps");
                startActivity(intent);
            }
        });
    }
}
