package com.haocent.android.googlemap;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment;
import com.google.android.gms.maps.model.LatLng;

/**
 * 街景
 *
 * Created by Tnno Wu on 2018/02/08.
 */

public class StreetViewActivity extends AppCompatActivity {

    private static final LatLng STATUE_OF_LIBERTY = new LatLng(40.689611718944896, -74.04564142227173);

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_street_view);

        SupportStreetViewPanoramaFragment streetViewPanoramaFragment =
                (SupportStreetViewPanoramaFragment)
                        getSupportFragmentManager().findFragmentById(R.id.streetviewpanorama);
        streetViewPanoramaFragment.getStreetViewPanoramaAsync(
                new OnStreetViewPanoramaReadyCallback() {
                    @Override
                    public void onStreetViewPanoramaReady(StreetViewPanorama panorama) {
                        if (savedInstanceState == null) {
                            panorama.setPosition(STATUE_OF_LIBERTY);
                        }
                    }
                });
    }
}
