package com.moremoregreen.androidnearbyplaces;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.moremoregreen.androidnearbyplaces.Model.Photos;
import com.moremoregreen.androidnearbyplaces.Model.PlaceDetail;
import com.moremoregreen.androidnearbyplaces.Remote.IGoogleAPIService;
import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Url;

public class ViewPlace extends AppCompatActivity {
    ImageView photo;
    RatingBar ratingBar;
    TextView opening_hour, place_address, place_name;
    Button btnViewOnMap, btnViewDirections;

    IGoogleAPIService mService;

    PlaceDetail mPlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_place);

        mService = Common.getGoogleAPIService();

        photo = findViewById(R.id.photo);
        ratingBar = findViewById(R.id.ratingBar);
        place_address = findViewById(R.id.place_address);
        place_name = findViewById(R.id.place_name);
        opening_hour = findViewById(R.id.place_open_hour);
        btnViewOnMap = findViewById(R.id.btn_show_map);
        btnViewDirections = findViewById(R.id.btn_view_directions);
        //Empty all view
        place_name.setText("");
        place_address.setText("");
        opening_hour.setText("");

        btnViewOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mPlace.getResult().getUrl()));
                startActivity(mapIntent);
            }
        });

        btnViewDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mapIntent = new Intent(ViewPlace.this, ViewDirections.class);
                startActivity(mapIntent);
            }
        });


        //Photo
        if(Common.currentResults.getPhotos() != null && Common.currentResults.getPhotos().length > 0)
        {
            Picasso.get()
                   .load(getPhotoOfPlace(Common.currentResults.getPhotos()[0].getPhoto_reference(),1000))  //getPhotos是陣列，所以我們要取第一個
                   .placeholder(R.drawable.ic_image_black_24dp)
                   .error(R.drawable.ic_error_black_24dp)
                   .into(photo);                                                                     //還要定義大小
        }

        //Rating
        if(Common.currentResults.getRating() != null && !TextUtils.isEmpty(Common.currentResults.getRating()))
        {
            ratingBar.setRating(Float.parseFloat(Common.currentResults.getRating()));
        }
        else
        {
            ratingBar.setVisibility(View.GONE);
        }

        //Opening_Hour
        if(Common.currentResults.getOpening_hours() != null)
        {
            if(Common.currentResults.getOpening_hours().getOpen_now() =="true" )
            opening_hour.setText("是否開門：開了喔");
            else
                opening_hour.setText("是否開門：還沒");
        }
        else
        {
            opening_hour.setVisibility(View.GONE);
        }

        //User service to fetch Address and Name
        mService.getDetailPlace(getPlaceDetailUrl(Common.currentResults.getPlace_id()))
                .enqueue(new Callback<PlaceDetail>() {
                    @Override
                    public void onResponse(Call<PlaceDetail> call, Response<PlaceDetail> response) {
                        mPlace = response.body();

                        place_address.setText(mPlace.getResult().getFormatted_address());
                        place_name.setText(mPlace.getResult().getName());

                    }

                    @Override
                    public void onFailure(Call<PlaceDetail> call, Throwable t) {

                    }
                });

    }

    private String getPlaceDetailUrl(String place_id) {
        StringBuilder url = new StringBuilder("https://maps.googleapis.com/maps/api/place/details/json");
        url.append("?placeid=" + place_id);
        url.append("&key=" + getResources().getString(R.string.browser_key));
        return url.toString();

    }

    private String getPhotoOfPlace(String photo_referrence, int maxWidth) {
        StringBuilder url = new StringBuilder("https://maps.googleapis.com/maps/api/place/photo");
        url.append("?maxwidth=" + maxWidth);
        url.append("&photoreference=" + photo_referrence);
        url.append("&key=" + getResources().getString(R.string.browser_key));
        return url.toString();

    }


}
