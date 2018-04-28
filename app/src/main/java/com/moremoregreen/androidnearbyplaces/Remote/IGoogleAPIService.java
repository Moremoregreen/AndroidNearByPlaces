package com.moremoregreen.androidnearbyplaces.Remote;

import com.moremoregreen.androidnearbyplaces.Model.MyPlaces;
import com.moremoregreen.androidnearbyplaces.Model.PlaceDetail;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface IGoogleAPIService {
    @GET
    Call<MyPlaces> getNearByPlaces(@Url String url);

    @GET
    Call<PlaceDetail> getDetailPlace(@Url String url);

    @GET("maps/api/directions/json")
    Call<String> getDiretions(@Query("origin") String origin, @Query("destination") String destination);
}
