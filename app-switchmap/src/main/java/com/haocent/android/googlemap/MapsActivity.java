package com.haocent.android.googlemap;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.model.LatLng;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;

/**
 * 国内国外地图切换
 *
 * 参考高德地图官方文档：http://lbs.amap.com/dev/demo/switch-map/#Android
 *
 * Created by Tnno Wu on 2018/02/08.
 */

public class MapsActivity extends FragmentActivity implements
        View.OnClickListener,
        AMap.OnCameraChangeListener,
        OnMapReadyCallback,
        GoogleMap.OnCameraMoveListener {

    private Button mMapBtn;
    private LinearLayout mContainerLayout;
    private LayoutParams mParams;
    private TextureMapView mAMapView;
    private MapView mGoogleMapView;

    private float zoom = 10;
    private double latitude = 39.23242;
    private double longitude = 116.253654;

    private boolean mIsAMapDisplay = true;
    private boolean mIsAuto = true;

    private GoogleMap googlemap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        initView();

        mAMapView = new TextureMapView(this);
        mParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        mContainerLayout.addView(mAMapView, mParams);

        mAMapView.onCreate(savedInstanceState);

        mAMapView.getMap().setOnCameraChangeListener(this);

        AlphaAnimation anAppear = new AlphaAnimation(0, 1);
        AlphaAnimation anDisappear = new AlphaAnimation(1, 0);
        anAppear.setDuration(5000);
        anDisappear.setDuration(5000);
    }

    private void initView() {
        mContainerLayout = findViewById(R.id.map_container);
        mMapBtn = findViewById(R.id.btn_switch);
        mMapBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_switch:
                mIsAuto = false;
                if (mIsAMapDisplay) {
                    changeToGoogleMapView();
                } else {
                    changeToAMapView();
                }
                break;
        }
    }

    /**
     * 切换为高德地图显示
     */
    private void changeToAMapView() {
        if (googlemap != null && googlemap.getCameraPosition() != null) {
            zoom = googlemap.getCameraPosition().zoom;
            latitude = googlemap.getCameraPosition().target.latitude;
            longitude = googlemap.getCameraPosition().target.longitude;
            mAMapView = new TextureMapView(this, new AMapOptions()
                    .camera(new com.amap.api.maps.model.CameraPosition(new LatLng(latitude, longitude), zoom, 0, 0)));
        } else {
            mAMapView = new TextureMapView(this);
        }
        mAMapView.onCreate(null);
        mAMapView.onResume();
        mContainerLayout.addView(mAMapView, mParams);

        mGoogleMapView.animate().alpha(0f).setDuration(500).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mGoogleMapView != null) {
                    mGoogleMapView.setVisibility(View.GONE);
                    mContainerLayout.removeView(mGoogleMapView);
                    mGoogleMapView.onDestroy();
                }
            }
        });
        mAMapView.getMap().setOnCameraChangeListener(this);
        mIsAMapDisplay = true;
        mMapBtn.setText("切换到谷歌地图");
    }


    private Handler handler = new Handler() {
        public void handleMessage(Message message) {
            mAMapView.setVisibility(View.GONE);
            mContainerLayout.removeView(mAMapView);
            if (mAMapView != null) {
                mAMapView.onDestroy();
            }
        }
    };

    /**
     * 切换为谷歌地图显示
     */
    private void changeToGoogleMapView() {
        zoom = mAMapView.getMap().getCameraPosition().zoom;
        latitude = mAMapView.getMap().getCameraPosition().target.latitude;
        longitude = mAMapView.getMap().getCameraPosition().target.longitude;

        mMapBtn.setText("切换到高德地图");
        mIsAMapDisplay = false;
        mGoogleMapView = new com.google.android.gms.maps.MapView(this, new GoogleMapOptions()
                .camera(new com.google.android.gms.maps.model
                        .CameraPosition(new com.google.android.gms.maps.model.LatLng(latitude, longitude), zoom, 0, 0)));
        mGoogleMapView.onCreate(null);
        mGoogleMapView.onResume();
        mContainerLayout.addView(mGoogleMapView, mParams);
        mGoogleMapView.getMapAsync(this);
        handler.sendEmptyMessageDelayed(0, 500);
    }

    @Override
    public void onCameraChange(com.amap.api.maps.model.CameraPosition cameraPosition) {

    }

    /**
     * 高德地图移动完成回调
     *
     * @param cameraPosition 地图移动结束的中心点位置信息
     */
    @Override
    public void onCameraChangeFinish(com.amap.api.maps.model.CameraPosition cameraPosition) {
        longitude = cameraPosition.target.longitude;
        latitude = cameraPosition.target.latitude;
        zoom = cameraPosition.zoom;
        if (!isInArea(latitude, longitude) && mIsAMapDisplay && mIsAuto) {
            changeToGoogleMapView();
        }
    }

    /**
     * 粗略判断当前屏幕显示的地图中心点是否在国内
     *
     * @param latitude  纬度
     * @param longitude 经度
     * @return 屏幕中心点是否在国内
     */
    private boolean isInArea(double latitude, double longitude) {
        return (latitude > 3.837031) && (latitude < 53.563624)
                && (longitude < 135.095670) && (longitude > 73.502355);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAMapView != null) {
            mAMapView.onResume();
        }
        if (mGoogleMapView != null) {
            mGoogleMapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAMapView != null) {
            mAMapView.onPause();
        }
        if (mGoogleMapView != null) {
            mGoogleMapView.onPause();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mAMapView != null) {
            mAMapView.onSaveInstanceState(outState);
        }
        if (mGoogleMapView != null) {
            mGoogleMapView.onSaveInstanceState(outState);
        }
    }


    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (mAMapView != null) {
            mAMapView.onDestroy();
        }
        if (mGoogleMapView != null) {
            mGoogleMapView.onDestroy();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googlemap = googleMap;
        if (googlemap != null) {
            googlemap.setOnCameraMoveListener(this);
        }
    }

    /**
     * 谷歌地图移动回调
     */
    @Override
    public void onCameraMove() {
        CameraPosition cameraPosition = googlemap.getCameraPosition();
        longitude = cameraPosition.target.longitude;
        latitude = cameraPosition.target.latitude;
        zoom = cameraPosition.zoom;
        if (isInArea(latitude, longitude) && !mIsAMapDisplay && mIsAuto) {
            changeToAMapView();
        }
    }
}
