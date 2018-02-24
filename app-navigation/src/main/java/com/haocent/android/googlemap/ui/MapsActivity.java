package com.haocent.android.googlemap.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
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
 * 导航
 *
 * 路线图 + 实时显示当前位置
 *
 * Created by Tnno Wu on 2018/02/24.
 */

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        View.OnClickListener {

    private static final String TAG = MapsActivity.class.getSimpleName();

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;

    private GoogleMap mMap;
    private LatLng mLatLng;
    private Marker mCurrentLocation;
    private double startLat, startLng, endLat, endLng;
    private boolean isNavigation;

    private GoogleDirectionService mService;

    private static String[] REQUEST_PERMISSION = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private TextView mTvStart, mTvEnd;
    private RelativeLayout mRlMap;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        initService();

        ActivityCompat.requestPermissions(this, REQUEST_PERMISSION, 666);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        initProgress();

        initView();
    }

    private void initService() {
        mService = new RetrofitClient().getService();
    }

    private void initProgress() {
        progressDialog = new ProgressDialog(this);
    }

    private void initView() {
        mTvStart = findViewById(R.id.tv_start);
        mTvEnd = findViewById(R.id.tv_end);
        TextView tvNavigation = findViewById(R.id.tv_navigation);
        Button mBtnPlan = findViewById(R.id.btn_plan);
        mRlMap = findViewById(R.id.rl_map);

        tvNavigation.setOnClickListener(this);
        mBtnPlan.setOnClickListener(this);
    }

    private void initMapData() {
        Observable<GoogleDirectionBean> observable = mService.getGoogleDirection(
                mTvStart.getText().toString(),
                mTvEnd.getText().toString(),
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

                        mRlMap.setVisibility(View.VISIBLE);
                    }
                });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        buildGoogleApiClient();

        mGoogleApiClient.connect();

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

    @Override
    protected void onPause() {
        super.onPause();

        if (mGoogleApiClient != null) {
            //noinspection deprecation
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //noinspection deprecation
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            mMap.clear();
            mLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(mLatLng);
            markerOptions.title("Current Position");
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 15));
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            mCurrentLocation = mMap.addMarker(markerOptions);
        }

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        //noinspection deprecation
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "onConnectionSuspended", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "onConnectionFailed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (mCurrentLocation != null) {
            mCurrentLocation.remove();
        }

        if (isNavigation) {
            Log.d(TAG, "onLocationChanged: ");

            hideProgress();

            mLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(mLatLng);
            markerOptions.title("Current Position");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            mCurrentLocation = mMap.addMarker(markerOptions);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, 15F));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_navigation:
                Log.d(TAG, "onClick: 导航");

                showProgress();

                isNavigation = true;
                break;
            case R.id.btn_plan:
                showProgress();

                mRlMap.setVisibility(View.GONE);

                initMapData();
                break;
            default:
                break;
        }
    }

    private void showProgress() {
        progressDialog.setMessage("路线规划中");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void hideProgress() {
        progressDialog.hide();
    }
}
