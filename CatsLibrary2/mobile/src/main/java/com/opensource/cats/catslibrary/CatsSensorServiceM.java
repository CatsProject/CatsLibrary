package com.opensource.cats.catslibrary;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by C on 2017-05-28.
 */

public class CatsSensorServiceM extends Service implements SensorEventListener, TagName {

    private SensorManager sensorManager;
    private SensorFilters sensorFilters;

    @Override
    public void onCreate() {
        super.onCreate();

        sensorFilters = new SensorFilters(true);
        startMeasurement();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i("unbind","할룽");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        stopMeasurement();
        super.onDestroy();
    }

    protected void startMeasurement() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(SENS_ACCELEROMETER);
        Sensor gyroscopeSensor = sensorManager.getDefaultSensor(SENS_GYROSCOPE);
        Sensor magneticSensor = sensorManager.getDefaultSensor(SENS_MAGNETIC);

        if (sensorManager != null) {
            if (accelerometerSensor != null) {
                sensorManager.registerListener(this, accelerometerSensor , SensorManager.SENSOR_DELAY_GAME);
            }
            if (gyroscopeSensor != null) {
                sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);
            }
            if (magneticSensor != null) {
                sensorManager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_GAME);
            }

        }
    }
    protected void stopMeasurement() {

        //sensorFiltering.stopTimer();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        sensorFilters.getSensor(event.values, event.sensor.getType(), event.timestamp);
        //sensorFiltering.getSensor(event.sensor.getType(), event.values, event.timestamp);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
