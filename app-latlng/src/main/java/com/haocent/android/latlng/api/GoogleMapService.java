package com.haocent.android.latlng.api;

import com.haocent.android.latlng.data.GoogleMapDataBean;

import io.reactivex.Observable;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * 参考 Google Map API 地理编码响应
 * https://developers.google.com/maps/documentation/geocoding/intro?hl=zh-cn
 *
 * Created by Tnno Wu on 2018/02/11.
 */

public interface GoogleMapService {

    // https://maps.googleapis.com/maps/api/geocode/json?address=%E5%A4%A7%E8%BF%9E&key=AIzaSyA3u4AmaB8OtJhsAM1mqW4wC8s3PKP9bXw&language=zh-CN
    @POST("/maps/api/geocode/json")
    Observable<GoogleMapDataBean> getData(@Query("address") String address,
                                          @Query("key") String key,
                                          @Query("language") String language);
}
