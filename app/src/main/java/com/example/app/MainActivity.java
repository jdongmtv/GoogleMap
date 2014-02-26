package com.example.app;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.graphics.Color;
import android.graphics.Interpolator;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import com.google.android.gms.maps.model.*;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import android.os.Bundle;
import android.os.Handler;
import android.util.Property;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.lang.Runnable;


public class MainActivity extends ActionBarActivity {

    final static LatLngInterpolator latLngInterpolator = new LatLngInterpolator.Spherical();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GoogleMap googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getUiSettings().setCompassEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setScrollGesturesEnabled(false);
        googleMap.getUiSettings().setRotateGesturesEnabled(false);
        googleMap.getUiSettings().setZoomGesturesEnabled(false);
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(1));
        googleMap.setMyLocationEnabled(false);
        LatLng startPosition = new LatLng(0,0);
        LatLng endPosition = new LatLng(0,40);
        final Marker marker = googleMap.addMarker(
                new MarkerOptions()
                    .position(startPosition).icon(BitmapDescriptorFactory.fromResource(R.drawable.plane)));
        googleMap.addMarker(new MarkerOptions()
               .position(startPosition));
        googleMap.addMarker(new MarkerOptions()
                .position(endPosition));
        Polyline line = googleMap.addPolyline(new PolylineOptions()
                .add(startPosition, endPosition)
                .width(1)
                .color(Color.RED).geodesic(true));
        animateMarkerToICS(marker, startPosition, endPosition);
    }

    private static void animateMarkerToICS(Marker marker, LatLng startPosition, final LatLng finalPosition) {
        TypeEvaluator<LatLng> typeEvaluator = new TypeEvaluator<LatLng>() {
            @Override
            public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
                return latLngInterpolator.interpolate(fraction, startValue, endValue);
            }
        };
        Property<Marker, LatLng> property = Property.of(Marker.class, LatLng.class, "position");
        ObjectAnimator animator = ObjectAnimator.ofObject(marker, property, typeEvaluator, finalPosition);
        animator.setDuration(30*1000);
        animator.start();
    }

}
