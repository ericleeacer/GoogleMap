package com.haocent.android.googlemap.api;

import com.haocent.android.googlemap.data.GoogleDirectionBean;

import io.reactivex.Observable;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by Tnno Wu on 2018/02/07.
 */

public interface GoogleDirectionService {

    /**
     * https://maps.googleapis.com/maps/api/directions/json?origin=Dalian&destination=Shenyang&key=AIzaSyCsSXwWlWn0JrjVtKAv1DWmrkTkI3sgrYs&mode=transit&language=zh-cn&sensor=true
     */
    @POST("/maps/api/directions/json")
    Observable<GoogleDirectionBean> getGoogleDirection(@Query("origin") String origin,
                                                       @Query("destination") String destination,
                                                       @Query("key") String key,
                                                       @Query("mode") String mode,
                                                       @Query("language") String language,
                                                       @Query("sensor") boolean sensor);
}
