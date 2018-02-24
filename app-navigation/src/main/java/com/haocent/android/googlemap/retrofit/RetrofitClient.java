package com.haocent.android.googlemap.retrofit;

import com.haocent.android.googlemap.api.GoogleDirectionService;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Tnno Wu on 2018/02/24.
 */

public class RetrofitClient {

    private static final String BASE_URL = "https://maps.googleapis.com";

    public GoogleDirectionService getService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        return retrofit.create(GoogleDirectionService.class);
    }
}
