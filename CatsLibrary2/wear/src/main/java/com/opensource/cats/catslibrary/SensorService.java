package com.opensource.cats.catslibrary;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;

import static com.opensource.cats.catslibrary.CatsWearableData.accData;
import static com.opensource.cats.catslibrary.CatsWearableData.gyroData;
import static com.opensource.cats.catslibrary.CatsWearableData.gyroTimeStamp;
import static com.opensource.cats.catslibrary.CatsWearableData.magnetData;

/**
 * Created by C on 2017-05-29
 */

public class SensorService extends Service implements SensorEventListener, TagName{


    SensorManager mSensorManager;

    private double temp[] =
                    {0.0, 0.0, 0.0,
                    0.0, 0.0, 0.0,
                    0.0, 0.0, 0.0};
    private double maro[] =
            {0.0, 0.0, 0.0,
            0.0, 0.0, 0.0,
            0.0, 0.0, 0.0};
    boolean hello =  false;
    int threshold = 20;

    int stacks;

    //SensorFiltering sensorFiltering;
    //MobileClient client;
    MobileClient  client;
    int halfSecond = 0;
    private Timer fuseTimer;

    long timestamp = 0;

    private float[] acc = new float[3];
    private float[] gyro = new float[3];
    private float[] magnet = new float[3];



    private ScheduledExecutorService mScheduler;

    @Override
    public void onCreate() {

        IntentFilter intentFilter = new IntentFilter(WEARABLE_BROADCAST);

        client = MobileClient.getInstance(getApplicationContext());




        super.onCreate();
       // sensorFiltering = new SensorFiltering(this, false);
                //SensorFiltering.getInstance(this, false);
        startMeasurement();
    }

    @Override
    public void onDestroy() {
        stopMeasurement();

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected void startMeasurement() {
        fuseTimer = new Timer();
        fuseTimer.scheduleAtFixedRate(new digitalSensor(), 0, TIME_CONSTANT);

        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
            Sensor accelerometerSensor = mSensorManager.getDefaultSensor(SENS_ACCELEROMETER);
            Sensor gyroscopeSensor = mSensorManager.getDefaultSensor(SENS_GYROSCOPE);
            Sensor magneticSensor = mSensorManager.getDefaultSensor(SENS_MAGNETIC);


        // Register the listener
        if (mSensorManager != null) {
            if (accelerometerSensor != null) {
                mSensorManager.registerListener(this, accelerometerSensor , SensorManager.SENSOR_DELAY_GAME);
            }
            if (gyroscopeSensor != null) {
                mSensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);
            }
            if (magneticSensor != null) {
                mSensorManager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_GAME);
            }
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    private void stopMeasurement() {
        fuseTimer.cancel();
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
        if (mScheduler != null && !mScheduler.isTerminated()) {
            mScheduler.shutdown();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) { //여기에서 DeviceClient 로 보내주는데..

        switch (event.sensor.getType()) {
            case SENS_ACCELEROMETER :
                acc = event.values;
                break;
            case SENS_GYROSCOPE :
                gyro = event.values;
                timestamp = event.timestamp;
                break;
            case SENS_MAGNETIC :
                magnet = event.values;
                break;
        }
        //client.sendSensorData();
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    public class digitalSensor extends TimerTask {

        @Override
        public void run() {

            if(!hello)
            {
                hello = true;

                temp[0] = acc[0];
                temp[1] = acc[1];
                temp[2] = acc[2];

                temp[3] = gyro[0];
                temp[4] = gyro[1];
                temp[5] = gyro[2];

                temp[6] = magnet[0];
                temp[7] = magnet[1];
                temp[8] = magnet[2];

            }
            else
            {
                maro[0] = temp[0] - acc[0];
                maro[1] = temp[1] - acc[1];
                maro[2] = temp[2] - acc[2];

                maro[3] = temp[3] - gyro[0];
                maro[4] = temp[4] - gyro[1];
                maro[5] = temp[5] - gyro[2];

                maro[6] = temp[6] - magnet[0];
                maro[7] = temp[7] - magnet[1];
                maro[8] = temp[8] - magnet[2];

                temp[0] = acc[0];
                temp[1] = acc[1];
                temp[2] = acc[2];

                temp[3] = gyro[0];
                temp[4] = gyro[1];
                temp[5] = gyro[2];

                temp[6] = magnet[0];
                temp[7] = magnet[1];
                temp[8] = magnet[2];
            }
            double sumMaro = CatsWearableController.getWeightAcc() * ((Math.abs(maro[0])+ Math.abs(maro[1])+ Math.abs(maro[2])))
                    +  CatsWearableController.getWeightGyro() * ((Math.abs(maro[3])+ Math.abs(maro[4])+ Math.abs(maro[5])))
                    +  CatsWearableController.getWeightMag() * ((Math.abs(maro[6])+ Math.abs(maro[7])+ Math.abs(maro[8])));


            gyroTimeStamp[halfSecond] = timestamp;
            if(sumMaro < CatsWearableController.getEchoThreshold())
            {
                stacks++;
            }
            else {
                stacks = 0;
            }
           if(stacks < CatsWearableController.getThresholdTime()) {
                for (int i = 0; i < 3; i++) {
                    accData[halfSecond * 3 + i] = acc[i];
                    gyroData[halfSecond * 3 + i] = gyro[i];
                    magnetData[halfSecond * 3 + i] = magnet[i];
                }


                halfSecond++;

                if (halfSecond >= 5) {
                    halfSecond = 0;

                    client.sendSensorData();
               }
            }
        }
    }
}