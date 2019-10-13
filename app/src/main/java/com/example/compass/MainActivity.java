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
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements SensorEventListener {

    @SuppressWarnings("unused")
    private String TAG = "SensorCompass";

    // Main View
    private LinearLayout mFrame;

    // Sensors & SensorManager
    private Sensor accelerometer;
    private Sensor magnetometer;
    private SensorManager mSensorManager;

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
        TextView status =findViewById(R.id.status);
        mFrame = findViewById(R.id.frame);
        Button exit_button = findViewById(R.id.btn_exit);
        mCompassArrow = new CompassArrowView(getApplicationContext());
        // Get a reference to the SensorManager
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Get a reference to the accelerometer
        accelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

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

            status.setText("Magnetometer and Accelerometer both sensors  not found cannot run");
            exit_button.setEnabled(true);

        } else if (null == magnetometer) {

            status.setText("Magnetometer not found cannot run");
            exit_button.setEnabled(true);


        } else if (null == accelerometer ) {


            status.setText("Accelerometer not found cannot run");
            exit_button.setEnabled(true);

        } else {

            mFrame.addView(mCompassArrow);
            status.setText("");
            exit_button.setEnabled(false);
            exit_button.setVisibility(View.INVISIBLE);

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
        Toast.makeText(this, "Accelerometer and Magnetometer Dis-engaged", Toast.LENGTH_LONG).show();

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
                R.drawable.compass_medium);
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
}
