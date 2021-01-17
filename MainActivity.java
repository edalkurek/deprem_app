package com.example.santral_v;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity implements SensorEventListener {


    private float lastX, lastY, lastZ;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private float deltaXMax = 0;
    private float deltaYMax = 0;
    private float deltaZMax = 0;

    private float deltaX = 0;
    private float deltaY = 0;
    private float deltaZ = 0;

    private float vibrateThreshold = 0;


    private WebView position;
    private TextView textInfo;


    public Vibrator v;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeViews();

        // getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        position.setWebChromeClient(new WebChromeClient());
        position = (WebView) findViewById(R.id.webView);
        position.getSettings().setJavaScriptEnabled(true);
        textInfo = (TextView) findViewById(R.id.textInfo);
        textInfo.setText("Location : ");
        LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //For Retrieving new Location
        LocationListener locListener = new MyLocationListener();
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                locListener);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // success! we have an accelerometer

            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            vibrateThreshold = 0.3f;

        } else {
            // fai! we dont have an accelerometer!
        }

        //initialize vibration
        v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        startHelloService();



    }




    public void initializeViews() {

    }

    //onResume() register the accelerometer for listening the events
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);

    }
    //onPause() unregister the accelerometer for stop listening the events
   /* protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    } */

    public void sound () {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        r.play();
        stopHelloService();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        // clean current values
        displayCleanValues();
        // display the current x,y,z accelerometer values
        displayCurrentValues();
        // display the max x,y,z accelerometer values
        displayMaxValues();

        // get the change of the x,y,z values of the accelerometer
        deltaX = Math.abs(lastX - event.values[0]);
        deltaY = Math.abs(lastY - event.values[1]);
        deltaZ = Math.abs(lastZ - event.values[2]);

        // if the change is below 2, it is just plain noise
        if (deltaX < 0.9)
            deltaX = 0;
        if (deltaY < 0.9)
            deltaY = 0;
        if (deltaZ < 15)
            deltaZ = 0;
        if ((deltaX>  vibrateThreshold) || (deltaY > vibrateThreshold) || (deltaZ > vibrateThreshold)) {
            v.vibrate(200);
            sound();


        }
    }



    public void displayCleanValues() {

    }

    // display the current x,y,z accelerometer values
    public void displayCurrentValues() {

    }

    public void startHelloService() {
        Intent intent = new Intent(getApplicationContext(),MyService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {

            startService(intent);
        }
    }

    public void stopHelloService() {
        Intent intent = new Intent(getApplicationContext(),MyService.class);
        stopService(intent);
    }

    // display the max x,y,z accelerometer values
    public void displayMaxValues() {
        if (deltaX > deltaXMax) {
            deltaXMax = deltaX;

        }
        if (deltaY > deltaYMax) {
            deltaYMax = deltaY;

        }
        if (deltaZ > deltaZMax) {
            deltaZMax = deltaZ;

        }



    }


    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            // Retrieving Latitude
            location.getLatitude();
            // Retrieving getLongitude
            location.getLongitude();

            textInfo.setText("");
            String text = "My Current Location is:\nLatitude = "
                    + location.getLatitude() + "\nLongitude = "
                    + location.getLongitude();
            textInfo.setText(text);
            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT)
                    .show();
            // set Google Map on webview
            String url = "http://maps.google.com/staticmap?center="
                    + location.getLatitude() + "," + location.getLongitude()
                    + "&zoom=14&size=512x512&maptype=mobile/&markers="
                    + location.getLatitude() + "," + location.getLongitude();
            position.loadUrl(url);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

            Toast.makeText(getApplicationContext(), "GPS Enabled",
                    Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onProviderDisabled(String provider) {

            Toast.makeText(getApplicationContext(), "GPS Disabled",
                    Toast.LENGTH_SHORT).show();

        }
    }
}

