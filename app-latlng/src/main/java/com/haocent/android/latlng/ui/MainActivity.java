package com.haocent.android.latlng.ui;

import android.app.ProgressDialog;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

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

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private GoogleMapService mService;

    ProgressDialog mProgressDialog;

    private TextInputEditText mEditText;
    private Button mBtn;
    private TextView mTvAddress, mTvLat, mTvLng;
    private LinearLayout mLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initService();

        initProgress();

        initView();
    }

    private void initService() {
        mService = new RetrofitClient().getService();
    }

    private void initProgress() {
        mProgressDialog = new ProgressDialog(this);
    }

    private void initView() {
        mEditText = findViewById(R.id.edit_input);
        mBtn = findViewById(R.id.btn);
        mTvAddress = findViewById(R.id.tv_formatted_address);
        mTvLat = findViewById(R.id.tv_lat);
        mTvLng = findViewById(R.id.tv_long);
        mLinearLayout = findViewById(R.id.ll);

        mLinearLayout.setVisibility(View.GONE);

        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress();

                initData();
            }
        });
    }

    private void showProgress() {
        mProgressDialog.setMessage("正在查询");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    private void initData() {
        Observable<GoogleMapDataBean> observable = mService.getData(
                mEditText.getText().toString(),
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

                        hideProgress();

                        mLinearLayout.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void hideProgress() {
        mProgressDialog.hide();
    }
}
