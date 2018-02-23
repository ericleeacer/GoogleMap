package com.haocent.android.latlng.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.haocent.android.latlng.R;
import com.haocent.android.latlng.api.GoogleMapService;
import com.haocent.android.latlng.data.Constant;
import com.haocent.android.latlng.data.GoogleMapDataBean;
import com.haocent.android.latlng.retrofit.RetrofitClient;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * 经纬度信息查询
 *
 * Created by Tnno Wu on 2018/02/11.
 */

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private GoogleMapService mService;

    private CardView mCardView;
    private TextView mTvAddress, mTvLat, mTvLng, mTvPlaceName;
    private Button mBtnSearch;
    private LinearLayout mLinearLayout;
    private ProgressBar mProgressBar;

    private String placeName;

    private int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initService();

        initView();
    }

    private void initService() {
        mService = new RetrofitClient().getService();
    }

    private void initView() {
        mCardView = findViewById(R.id.card_view);
        mBtnSearch = findViewById(R.id.btn);
        mTvAddress = findViewById(R.id.tv_formatted_address);
        mTvLat = findViewById(R.id.tv_lat);
        mTvLng = findViewById(R.id.tv_long);
        mTvPlaceName = findViewById(R.id.tv_place_name);
        mLinearLayout = findViewById(R.id.ll);
        mProgressBar = findViewById(R.id.progress);

        mBtnSearch.setVisibility(View.GONE);
        mLinearLayout.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);

        mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // 参考：地点自动填充 https://developers.google.com/places/android-api/autocomplete?hl=zh-cn
                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).build(MainActivity.this);
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        mBtnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBar.setVisibility(View.VISIBLE);

                initData();
            }
        });
    }

    private void initData() {
        Observable<GoogleMapDataBean> observable = mService.getData(
                placeName,
                Constant.GOOGLE_API_KEY,
                Constant.LANGUAGE
        );
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<GoogleMapDataBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.d(TAG, "onSubscribe: ");
                    }

                    @Override
                    public void onNext(GoogleMapDataBean googleMapDataBean) {
                        Log.d(TAG, "onNext: ");

                        mTvAddress.setText(googleMapDataBean.getResults().get(0).getFormatted_address());
                        mTvLat.setText(googleMapDataBean.getResults().get(0).getGeometry().getLocation().getLat() + "");
                        mTvLng.setText(googleMapDataBean.getResults().get(0).getGeometry().getLocation().getLng() + "");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: ");
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");

                        mProgressBar.setVisibility(View.GONE);

                        mLinearLayout.setVisibility(View.VISIBLE);
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                placeName = place.getName().toString();

                if (TextUtils.isEmpty(placeName)) {
                    mTvPlaceName.setText("输入查询的地点");
                    mBtnSearch.setVisibility(View.GONE);
                } else {
                    mTvPlaceName.setText(placeName);
                    mBtnSearch.setVisibility(View.VISIBLE);
                }
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.d(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {

            }
        }
    }
}
