package com.capstone.knockknock;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;


/**
 * Detects spikes in accelerometer data (only in z axis) and generates accelerometer events
 * the volatile spikeDetected boolean will be set when a spike is detected
 */
public class AccelSpikeDetector implements SensorEventListener {

    private SensorManager mSensorManager;

    private Callback callbackMethod;

    // Forces are in m/s^2
    final public float minAccelZ = 5;
    final public float minAccelX = 5;
    final public float minAccelY = 5;

    //For high pass filter
    private float currentAccelZ = 0;
    private float currentAccelX = 0;
    private float currentAccelY = 0;

    AccelSpikeDetector(SensorManager sm){
        mSensorManager = sm;
    }

    public void registerCallback(Callback cb){
        callbackMethod = cb;
    }

    public void unregisterCallback(){
        callbackMethod = null;
    }

    public void stopAccSensing(){
        mSensorManager.unregisterListener(this);
    }

    public void resumeAccSensing(){
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), mSensorManager.SENSOR_DELAY_FASTEST);
    }

    public void onSensorChanged(SensorEvent event) {
        currentAccelX = Math.abs(event.values[0]); // X-axis
        currentAccelY = Math.abs(event.values[1]); // Y-axis
        currentAccelZ = Math.abs(event.values[2]); // Z-axis

        // Z force must be above some limit, the other forces below some limit to filter out shaking motions
        if (currentAccelZ > minAccelZ && currentAccelX < minAccelX && currentAccelY < minAccelY){
            String log = "currXVal:" + currentAccelX + " currYVal:" + currentAccelY + " currZVal:" + currentAccelZ;
            Log.d("AccelSpikeDetector", "onSensorChanged " + log);
            callbackMethod.knockEvent();
        }

    }

    // Must override to implement SensorEventListener
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}