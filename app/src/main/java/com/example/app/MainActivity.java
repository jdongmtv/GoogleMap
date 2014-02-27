package com.example.app;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.graphics.Color;
import android.graphics.Interpolator;
import android.os.Message;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.view.animation.AccelerateDecelerateInterpolator;
import java.util.ArrayList;
import java.lang.Runnable;


public class MainActivity extends ActionBarActivity {

    private final static String TAG = MainActivity.class.getSimpleName();
    private final static LatLngInterpolator latLngInterpolator = new LatLngInterpolator.Spherical();
    private final static TypeEvaluator<LatLng> typeEvaluator = new TypeEvaluator<LatLng>() {
        @Override
        public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
            return latLngInterpolator.interpolate(fraction, startValue, endValue);
        }
    };
    private final static Property<Marker, LatLng> property = Property.of(Marker.class, LatLng.class, "position");
    private final static long DURATION = 1000;

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
        final LatLng[]  line  =  bezier(startPosition, endPosition, 10, 0.2, true);
        googleMap.addPolyline(new PolylineOptions()
                .add(bezier(startPosition, endPosition, 10, 0.2, true))
                .width(1)
                .color(Color.RED).geodesic(true));
        animateMarkerToICS(marker, 0, line);
    }

    private static void animateMarkerToICS(final Marker marker, final int current,final LatLng[] line) {
        if(line == null || line.length == 0 || current >= line.length)
            return;

        ObjectAnimator animator = ObjectAnimator.ofObject(marker, property, typeEvaluator, line[current]);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animateMarkerToICS(marker, current+1, line);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.setDuration(DURATION);
        animator.start();

    }
    public static LatLng[] bezier(LatLng p1, LatLng p2, double arcHeight, double skew, boolean up) {
        ArrayList<LatLng> list = new ArrayList<LatLng>();
        try {
            if(p1.longitude > p2.longitude){
                LatLng tmp = p1;
                p1 = p2;
                p2 = tmp;
            }

            LatLng c = new LatLng((p1.latitude + p2.latitude)/2 , (p1.longitude + p2.longitude)/2);

            double cLat = c.latitude;
            double cLon = c.longitude;


            //add skew and arcHeight to move the midPoint
            if(Math.abs(p1.longitude - p2.longitude) < 0.0001){
                if(up){
                    cLon -= arcHeight;
                }else{
                    cLon += arcHeight;
                    cLat += skew;
                }
            }else{
                if(up){
                    cLat += arcHeight;
                }else{
                    cLat -= arcHeight;
                    cLon += skew;
                }
            }

            list.add(p1);
            //calculating points for bezier
            double tDelta = 1.0/10;
            CartesianCoordinates cart1 = new CartesianCoordinates(p1);
            CartesianCoordinates cart2 = new CartesianCoordinates(p2);
            CartesianCoordinates cart3 = new CartesianCoordinates(cLat, cLon);

            for (double t = 0; t <= 1.0; t += tDelta) {
                double oneMinusT = (1.0 - t);
                double t2 = Math.pow(t, 2);

                double y = oneMinusT * oneMinusT * cart1.y + 2 * t * oneMinusT * cart3.y + t2 * cart2.y;
                double x = oneMinusT * oneMinusT * cart1.x + 2 * t * oneMinusT * cart3.x + t2 * cart2.x;
                double z = oneMinusT * oneMinusT * cart1.z + 2 * t * oneMinusT * cart3.z + t2 * cart2.z;
                LatLng control = CartesianCoordinates.toLatLng(x, y, z);
                list.add(control);
            }

            list.add(p2);
        } catch (Exception e) {
            Log.e(TAG, "bezier", e);
        }
        LatLng[] result = new LatLng[list.size()];
        result = list.toArray(result);
        return result;
    }

    private static class CartesianCoordinates {
        private static final int R = 6371; // approximate radius of earth
        double x;
        double y;
        double z;

        public CartesianCoordinates(LatLng p) {
            this(p.latitude, p.longitude);
        }

        public CartesianCoordinates(double lat, double lon) {
            double _lat = Math.toRadians(lat);
            double _lon = Math.toRadians(lon);

            x = R * Math.cos(_lat) * Math.cos(_lon);
            y = R * Math.cos(_lat) * Math.sin(_lon);
            z = R * Math.sin(_lat);
        }

        public static LatLng toLatLng(double x, double y, double z) {
            return new LatLng(Math.toDegrees(Math.asin(z / R)), Math.toDegrees(Math.atan2(y, x)));
        }
    }
}
