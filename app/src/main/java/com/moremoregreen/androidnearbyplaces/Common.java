package com.moremoregreen.androidnearbyplaces;

import com.moremoregreen.androidnearbyplaces.Model.MyPlaces;
import com.moremoregreen.androidnearbyplaces.Model.Results;
import com.moremoregreen.androidnearbyplaces.Remote.IGoogleAPIService;
import com.moremoregreen.androidnearbyplaces.Remote.RetrofitClient;
import com.moremoregreen.androidnearbyplaces.Remote.RetrofitScalarsClient;

import retrofit2.Retrofit;

public class Common {
    public static Results currentResults;
    private static final String GOOGLE_API_URL = "https://maps.googleapis.com/";
    public static IGoogleAPIService getGoogleAPIService()
    {
        return RetrofitClient.getClient(GOOGLE_API_URL).create(IGoogleAPIService.class);
    }

    public static IGoogleAPIService getGoogleAPIServiceScalars()
    {
        return RetrofitScalarsClient.getScalarClient(GOOGLE_API_URL).create(IGoogleAPIService.class);
    }
}
