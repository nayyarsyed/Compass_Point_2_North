package com.example.compass;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazon.device.ads.AdLayout;
import com.amazon.device.ads.AdRegistration;


public class MainActivity extends Activity implements SensorEventListener {


    private String TAG = "SensorCompass";
    public int both;


    private AdLayout adView;
    private static final String APP_KEY = "sample-app-v1_pub-2"; // Sample Application Key. Replace this value with your Application Key.
    private static final String LOG_TAG = "SimpleAdSample"; // Tag used to prefix all log messages.

    // Main View
    private LinearLayout mFrame;
    // Sensors & SensorManager
    private Sensor accelerometer;
    private Sensor magnetometer;
    private SensorManager mSensorManager;
    public TextView status;

    // Storage for Sensor readings
    private float[] mGravity = null;
    private float[] mGeomagnetic = null;
    // Rotation around the Z axis
    private double mRotationInDegress;
    // View showing the compass arrow
    private CompassArrowView mCompassArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // For debugging purposes enable logging, but disable for production builds.
        AdRegistration.enableLogging(true);
        // For debugging purposes flag all ad requests as tests, but set to false for production builds.
        AdRegistration.enableTesting(true);

        this.adView = (AdLayout) findViewById(R.id.ad_view);
        //this.adView.setListener(new SampleAdListener());



        try {
            AdRegistration.setAppKey(APP_KEY);
        } catch (final IllegalArgumentException e) {
            Log.e(LOG_TAG, "IllegalArgumentException thrown: " + e.toString());
            return;
        }
        loadAd();






        mFrame = findViewById(R.id.jframe);
        Button exit_button = findViewById(R.id.btn_exit);
        status =findViewById(R.id.status);
        mCompassArrow = new CompassArrowView(getApplicationContext());
        // Get a reference to the SensorManager
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Get a reference to the accelerometer
        accelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // ad placement
       /* MobileAds.initialize(this,"ca-app-pub-6944210613333060~1927348080");
        AdView compass_ad = findViewById(R.id.adView);
        AdRequest comp_adreq = new AdRequest.Builder().build();
        compass_ad.loadAd(comp_adreq);*/

        // Get a reference to the magnetometer
        magnetometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

//        accelerometer = null;
//        magnetometer = null;

        exit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View exit_button_view) {
                  finish();
            }
        });

        // Exit unless both sensors are available
        if (null == accelerometer & null == magnetometer ) {

            status.setText("Magnetometer and Accelerometer both sensors not found. Application cannot run.");
            exit_button.setEnabled(true);
            both = 1;

        } else if (null == magnetometer) {

            status.setText("Accelerometer found but Magnetometer is not found in this phone which is required so cannot run the app");
            exit_button.setEnabled(true);

        } else if (null == accelerometer ) {

            status.setText("Magnetometer found but Accelerometer  is not found in this phone which is required so cannot run the app");
            exit_button.setEnabled(true);

            }

        else

            {

            mFrame.addView(mCompassArrow);

        }
        }




    @Override
    protected void onResume() {
        super.onResume();


        // Register for sensor updates

        mSensorManager.registerListener(this, accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);

        mSensorManager.registerListener(this, magnetometer,
                SensorManager.SENSOR_DELAY_NORMAL);


    }

    @Override
    protected void onPause() {
        super.onPause();

        // Unregister all sensors
        mSensorManager.unregisterListener(this);
        if (both ==0) {
            Toast.makeText(this, "Acquired Sensors Dis-engaged", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        // Acquire accelerometer event data

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            mGravity = new float[3];
            System.arraycopy(event.values, 0, mGravity, 0, 3);

        }

        // Acquire magnetometer event data

        else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {

            mGeomagnetic = new float[3];
            System.arraycopy(event.values, 0, mGeomagnetic, 0, 3);

        }

        // If we have readings from both sensors then
        // use the readings to compute the device's orientation
        // and then update the display.

        if (mGravity != null && mGeomagnetic != null) {

            float rotationMatrix[] = new float[9];

            // Users the accelerometer and magnetometer readings
            // to compute the device's rotation with respect to
            // a real world coordinate system

            boolean success = SensorManager.getRotationMatrix(rotationMatrix,
                    null, mGravity, mGeomagnetic);

            if (success) {

                float orientationMatrix[] = new float[3];

                status.setText("Accelerometer & Magnetometer acquired");


                // Returns the device's orientation given
                // the rotationMatrix

                SensorManager.getOrientation(rotationMatrix, orientationMatrix);

                // Get the rotation, measured in radians, around the Z-axis
                // Note: This assumes the device is held flat and parallel
                // to the ground

                float rotationInRadians = orientationMatrix[0];

                // Convert from radians to degrees
                mRotationInDegress = Math.toDegrees(rotationInRadians);

                // Request redraw
                mCompassArrow.invalidate();

                // Reset sensor event data arrays
                mGravity = mGeomagnetic = null;

            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // N/A
    }

    public class CompassArrowView extends View {

        Bitmap mBitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.arrow);
        int mBitmapWidth = mBitmap.getWidth();

        // Height and Width of Main View
        int mParentWidth;
        int mParentHeight;

        // Center of Main View
        int mParentCenterX;
        int mParentCenterY;

        // Top left position of this View
        int mViewTopX;
        int mViewLeftY;

        public CompassArrowView(Context context) {
            super(context);
        };

        // Compute location of compass arrow
        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            mParentWidth = mFrame.getWidth();
            mParentHeight = mFrame.getHeight();

            mParentCenterX = mParentWidth / 2;
            mParentCenterY = mParentHeight / 2;

            mViewLeftY = mParentCenterX - mBitmapWidth / 2;
            mViewTopX = mParentCenterY - mBitmapWidth / 2;
        }

        // Redraw the compass arrow
        @Override
        protected void onDraw(Canvas canvas) {

            // Save the canvas
            canvas.save();

            // Rotate this View
            canvas.rotate((float) -mRotationInDegress, mParentCenterX,
                    mParentCenterY);

            // Redraw this View
            canvas.drawBitmap(mBitmap, mViewLeftY, mViewTopX, null);

            // Restore the canvas
            canvas.restore();

        }
    }

    public void loadAd() {
        // Load an ad with default ad targeting.
        this.adView.loadAd();

        // Note: You can choose to provide additional targeting information to modify how your ads
        // are targeted to your users. This is done via an AdTargetingOptions parameter that's passed
        // to the loadAd call. See an example below:
        //
        //    final AdTargetingOptions adOptions = new AdTargetingOptions();
        //    adOptions.enableGeoLocation(true);
        //    this.adView.loadAd(adOptions);
    }

}
