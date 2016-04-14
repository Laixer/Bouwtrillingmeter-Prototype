package com.gemeenterotterdam.bouwtrillingsmeter;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.support.v4.app.Fragment;

/**
 * Created by yoric on 4-4-2016.
 */
public class Measurement extends Service implements SensorEventListener {

    private static Context aContext = null;
    private SensorManager sensorManager;
    private Fragment callback;

    public Measurement(Context context) {
        aContext = context;
        if (aContext != null) {
            sensorManager = (SensorManager) aContext.getSystemService(SENSOR_SERVICE);
            sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), 10000);
        }
    }

    public void setCallback(Fragment fr) {
        callback = fr;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        ((Graph.PlaceholderFragment)callback).onMeasurementUpdate(event.values[0], event.values[1], event.values[2]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
