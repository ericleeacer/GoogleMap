package com.haocent.android.latlng.retrofit;

import com.haocent.android.latlng.api.GoogleMapService;
import com.haocent.android.latlng.data.Constant;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Tnno Wu on 2018/02/11.
 */

public class RetrofitClient {

    public GoogleMapService getService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constant.GOOGLE_MAP_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        return retrofit.create(GoogleMapService.class);
    }
}
