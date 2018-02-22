package com.haocent.android.googlemap.ui;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.haocent.android.googlemap.R;
import com.haocent.android.googlemap.api.GoogleDirectionService;
import com.haocent.android.googlemap.data.Constant;
import com.haocent.android.googlemap.data.GoogleDirectionBean;
import com.haocent.android.googlemap.retrofit.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * 路线图
 *
 * Created by Tnno Wu on 2018/02/07.
 */

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = MapsActivity.class.getSimpleName();

    private GoogleMap mMap;

    private GoogleDirectionService mService;

    private MapsAdapter mAdapter;

    private TextInputEditText mEtStart, mEtEnd;
    private TextView mTvDistance, mTvDuration;

    private String totalDistance, totalDuration;

    ProgressDialog progressDialog;

    private List<GoogleDirectionBean.RoutesBean.LegsBean.StepsBean> mList;

    private double startLat, startLng, endLat, endLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        initService();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        initView();

        initProgress();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                mMap.addMarker(new MarkerOptions().position(new LatLng(startLat, startLng)));
                mMap.addMarker(new MarkerOptions().position(new LatLng(endLat, endLng)));

                LatLng latLngStart = new LatLng(startLat, startLng);
                LatLng latLngEnd = new LatLng(endLat, endLng);
                LatLngBounds bounds = new LatLngBounds.Builder().include(latLngStart).include(latLngEnd).build();
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 100);
                mMap.moveCamera(cameraUpdate);
            }
        });
    }

    private void initService() {
        mService = new RetrofitClient().getService();
    }

    private void initView() {
        mAdapter = new MapsAdapter(this);

        mEtStart = findViewById(R.id.edit_start);
        mEtEnd = findViewById(R.id.edit_end);

        Button btnDirection = findViewById(R.id.btn_direction);
        btnDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress();

                initMapData();
            }
        });

        mTvDistance = findViewById(R.id.tv_total_distance);
        mTvDuration = findViewById(R.id.tv_total_duration);

        RecyclerView rcv = findViewById(R.id.rcv_map);
        rcv.setLayoutManager(new LinearLayoutManager(this));
        rcv.setHasFixedSize(false);
        rcv.setAdapter(mAdapter);
    }

    private void initMapData() {
        Observable<GoogleDirectionBean> observable = mService.getGoogleDirection(
                mEtStart.getText().toString(),
                mEtEnd.getText().toString(),
                Constant.GOOGLE_API_KEY,
                Constant.TRAVEL_MODE,
                Constant.LANGUAGE,
                true
        );
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<GoogleDirectionBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.d(TAG, "onSubscribe: ");
                    }

                    @Override
                    public void onNext(GoogleDirectionBean googleDirectionBean) {
                        Log.d(TAG, "onNext: ");

                        mAdapter.setData(googleDirectionBean);

                        totalDistance = googleDirectionBean.getRoutes().get(0).getLegs().get(0).getDistance().getText();
                        totalDuration = googleDirectionBean.getRoutes().get(0).getLegs().get(0).getDuration().getText();

                        mTvDistance.setText("全程：" + totalDistance);
                        mTvDuration.setText("预计：" + totalDuration);

                        mList = googleDirectionBean.getRoutes().get(0).getLegs().get(0).getSteps();
                        Log.d(TAG, "onNext: " + mList.size());

                        startLat = googleDirectionBean.getRoutes().get(0).getLegs().get(0).getStart_location().getLat();
                        startLng = googleDirectionBean.getRoutes().get(0).getLegs().get(0).getStart_location().getLng();

                        endLat = googleDirectionBean.getRoutes().get(0).getLegs().get(0).getEnd_location().getLat();
                        endLng = googleDirectionBean.getRoutes().get(0).getLegs().get(0).getEnd_location().getLng();

                        // 画路线图
                        drawRoute(googleDirectionBean);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: ");
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");

                        hideProgress();

                        onMapReady(mMap);
                    }
                });
    }

    private void initProgress() {
        progressDialog = new ProgressDialog(this);
    }

    private void showProgress() {
        progressDialog.setMessage("路线规划中");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void hideProgress() {
        progressDialog.hide();
    }

    /**
     * 画路线图
     */
    private void drawRoute(GoogleDirectionBean googleDirectionBean) {
        adjustBounds(decodePoly(googleDirectionBean.getRoutes().get(0).getOverview_polyline().getPoints()));
    }

    private void adjustBounds(List<LatLng> listPolyline) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng latLng : listPolyline) {
            builder.include(latLng);
        }
        LatLngBounds bounds = builder.build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 2);
        mMap.animateCamera(cameraUpdate);

        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.RED);
        polylineOptions.width(5);
        polylineOptions.startCap(new SquareCap());
        polylineOptions.endCap(new SquareCap());
        polylineOptions.jointType(JointType.ROUND);
        polylineOptions.addAll(listPolyline);
        mMap.addPolyline(polylineOptions);
        mMap.addMarker(new MarkerOptions().position(listPolyline.get(listPolyline.size() - 1)));
    }

    private List decodePoly(String encoded) {
        List poly = new ArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
}
