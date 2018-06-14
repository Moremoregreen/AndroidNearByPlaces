package com.moremoregreen.androidnearbyplaces.Model;

import com.google.android.gms.nearby.messages.Distance;

import java.util.List;

public class Leg {
    public Distance distance;
    public Duration duration;
    public String end_address;
    public EndLocation end_location ;
    public String start_address ;
    public StartLocation start_location ;
    public List<Step> steps;
    public List<Object> traffic_speed_entry ;
    public List<Object> via_waypoint ;
}
