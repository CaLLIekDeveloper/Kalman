/*
 * MainActivity
 *
 * Copyright (c) 2014 Renato Villone
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.calliek.kalman;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;
import android.widget.Toast;

import com.calliek.kalman.Permissions;
import com.calliek.kalman.lib.KalmanLocationManager;

import static com.calliek.kalman.lib.KalmanLocationManager.UseProvider;

public class MainActivity extends Activity {


    // Constant

    /**
     * Request location updates with the highest possible frequency on gps.
     * Typically, this means one update per second for gps.
     */
    private static final long GPS_TIME = 1000;

    /**
     * For the network provider, which gives locations with less accuracy (less reliable),
     * request updates every 5 seconds.
     */
    private static final long NET_TIME = 5000;

    /**
     * For the filter-time argument we use a "real" value: the predictions are triggered by a timer.
     * Lets say we want 5 updates (estimates) per second = update each 200 millis.
     */
    private static final long FILTER_TIME = 200;

    // Context
    private KalmanLocationManager mKalmanLocationManager;

    // UI elements
    private TextView tvGps;
    private TextView tvNet;
    private TextView tvKal;
    private TextView tvAlt;

    private TextView tvLong;
    private TextView tvLat;

    private TextView tvLongK;
    private TextView tvLatK;

    // Textview animation
    private Animation mGpsAnimation;
    private Animation mNetAnimation;
    private Animation mKalAnimation;

    // GoogleMaps own OnLocationChangedListener (not android's LocationListener)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Permissions.requestPermissions(this);
        // Context
        mKalmanLocationManager = new KalmanLocationManager(this);


        // UI elements


        tvGps = (TextView) findViewById(R.id.tvGps);
        tvNet = (TextView) findViewById(R.id.tvNet);
        tvKal = (TextView) findViewById(R.id.tvKal);
        tvAlt = (TextView) findViewById(R.id.tvAlt);

        tvLat = findViewById(R.id.tvLat);
        tvLong = findViewById(R.id.tvLong);

        tvLatK = findViewById(R.id.tvLatK);
        tvLongK = findViewById(R.id.tvLongK);




        // TextView animation
        final float fromAlpha = 1.0f, toAlpha = 0.5f;

        mGpsAnimation = new AlphaAnimation(fromAlpha, toAlpha);
        mGpsAnimation.setDuration(GPS_TIME / 2);
        mGpsAnimation.setFillAfter(true);
        tvGps.startAnimation(mGpsAnimation);

        mNetAnimation = new AlphaAnimation(fromAlpha, toAlpha);
        mNetAnimation.setDuration(NET_TIME / 2);
        mNetAnimation.setFillAfter(true);
        tvNet.startAnimation(mNetAnimation);

        mKalAnimation = new AlphaAnimation(fromAlpha, toAlpha);
        mKalAnimation.setDuration(FILTER_TIME / 2);
        mKalAnimation.setFillAfter(true);
        tvKal.startAnimation(mKalAnimation);

        // Init altitude textview
        tvAlt.setText(getString(R.string.activity_main_fmt_alt, "-"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_settings:

                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                return true;

            default:

                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Request location updates with the highest possible frequency on gps.
        // Typically, this means one update per second for gps.

        // For the network provider, which gives locations with less accuracy (less reliable),
        // request updates every 5 seconds.

        // For the filtertime argument we use a "real" value: the predictions are triggered by a timer.
        // Lets say we want 5 updates per second = update each 200 millis.

        mKalmanLocationManager.requestLocationUpdates(
                UseProvider.GPS_AND_NET, FILTER_TIME, GPS_TIME, NET_TIME, mLocationListener, true);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Remove location updates
        mKalmanLocationManager.removeUpdates(mLocationListener);

    }

    /**
     * Listener used to get updates from KalmanLocationManager (the good old Android LocationListener).
     */
    private LocationListener mLocationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {


            // GPS location
            if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {

                tvLat.setText("Ширина: "+location.getLatitude());
                tvLong.setText("Долгота: "+location.getLongitude());

                tvGps.clearAnimation();
                tvGps.startAnimation(mGpsAnimation);
            }

            // Network location
            if (location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {


                tvNet.clearAnimation();
                tvNet.startAnimation(mNetAnimation);
            }

            // If Kalman location and google maps activated the supplied mLocationSource
            if (location.getProvider().equals(KalmanLocationManager.KALMAN_PROVIDER)) {
                // Update altitude
                String altitude = location.hasAltitude() ? String.format("%.1f", location.getAltitude()) : "-";
                tvAlt.setText(getString(R.string.activity_main_fmt_alt, altitude));

                tvLatK.setText("Ширина К: "+location.getLatitude());
                tvLongK.setText("Долгота К: "+location.getLongitude());

                // Animate textview
                tvKal.clearAnimation();
                tvKal.startAnimation(mKalAnimation);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

            String statusString = "Unknown";

            switch (status) {

                case LocationProvider.OUT_OF_SERVICE:
                    statusString = "Out of service";
                    break;

                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    statusString = "Temporary unavailable";
                    break;

                case LocationProvider.AVAILABLE:
                    statusString = "Available";
                    break;
            }

            Toast.makeText(
                    MainActivity.this,
                    String.format("Provider '%s' status: %s", provider, statusString),
                    Toast.LENGTH_SHORT)
            .show();
        }

        @Override
        public void onProviderEnabled(String provider) {

            Toast.makeText(
                    MainActivity.this, String.format("Provider '%s' enabled", provider), Toast.LENGTH_SHORT).show();

            // Remove strike-thru in label
            if (provider.equals(LocationManager.GPS_PROVIDER)) {

                tvGps.setPaintFlags(tvGps.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                tvGps.invalidate();
            }

            if (provider.equals(LocationManager.NETWORK_PROVIDER)) {

                tvNet.setPaintFlags(tvNet.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                tvNet.invalidate();
            }
        }

        @Override
        public void onProviderDisabled(String provider) {

            Toast.makeText(
                    MainActivity.this, String.format("Provider '%s' disabled", provider), Toast.LENGTH_SHORT).show();

            // Set strike-thru in label and hide accuracy circle
            if (provider.equals(LocationManager.GPS_PROVIDER)) {

                tvGps.setPaintFlags(tvGps.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvGps.invalidate();
            }

            if (provider.equals(LocationManager.NETWORK_PROVIDER)) {

                tvNet.setPaintFlags(tvNet.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvNet.invalidate();
            }
        }
    };

}
