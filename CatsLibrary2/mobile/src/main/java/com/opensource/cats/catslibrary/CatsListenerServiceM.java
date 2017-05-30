package com.opensource.cats.catslibrary;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.Timer;

import static com.opensource.cats.catslibrary.Data.accFromWear;
import static com.opensource.cats.catslibrary.Data.checkSend;
import static com.opensource.cats.catslibrary.Data.fuseTimer;
import static com.opensource.cats.catslibrary.Data.gyroFromWear;
import static com.opensource.cats.catslibrary.Data.isAlreadySend;
import static com.opensource.cats.catslibrary.Data.magnetFromWear;
import static com.opensource.cats.catslibrary.Data.sending;

/**
 * Created by C on 2017-05-29.
 */

public class CatsListenerServiceM  extends WearableListenerService implements TagName {

    SensorFilters sensorFilters = new SensorFilters(false);

    @Override
    public void onMessageReceived(MessageEvent messageEvent) { //어차피 안 와요.

        if (messageEvent.getPath().equals(SEND_MOBILE)) {
            final String message = new String(messageEvent.getData());

            // Broadcast message to wearable activity for display
            Intent messageIntent = new Intent();
            messageIntent.putExtra("message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
        } else {
            super.onMessageReceived(messageEvent);
        }
    }


    @Override
    public void onDataChanged(DataEventBuffer dataEvents) { //데이터 시간마다 받는 함수인듯,
        //Log.d(TAG, "onDataChanged()");


        for (DataEvent dataEvent : dataEvents) {
            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                DataItem dataItem = dataEvent.getDataItem();
                Uri uri = dataItem.getUri();
                String path = uri.getPath();

                // Log.i("hello", ""+path );
                if (path.startsWith(SEND_MOBILE)) {
                    unpackSensorData( // 밑에 있는 코드로 보내줌..
                            // DataType
                            DataMapItem.fromDataItem(dataItem).getDataMap()
                    );
                }
            }
        }
    }

    protected void unpackSensorData(DataMap dataMap) { //아마 직접 만든 코드인것 같음.

        sending++;

        float[] d1 = dataMap.getFloatArray(ACCELEROMETER);
        float[] d2 = dataMap.getFloatArray(GYROSCOPE);
        float[] d3 = dataMap.getFloatArray(MAGNETIC);
        long[] time = dataMap.getLongArray(TIMESTAMP);
        long[] gyroTime = dataMap.getLongArray(GYROSCOPE_TIME);

        ;
        Log.i("ListenerService", d1[0] + " " + d1[3] + " " + d1[6] + " " + d1[9] + " " + d1[12]);

        try {
            System.arraycopy(d1, 0, accFromWear, 0, 15);
            System.arraycopy(d2, 0, gyroFromWear, 0, 15);
            System.arraycopy(d3, 0, magnetFromWear, 0, 15);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 5; i++) {
            d1 = new float[3];
            d2 = new float[3];
            d3 = new float[3];

            for (int j = 0; j < 3; j++) {
                d1[j] = accFromWear[(i * 3) + j];
                d2[j] = gyroFromWear[(i * 3) + j];
                d3[j] = magnetFromWear[(i * 3) + j];
            }
            sendToSensorFilter(d1, d2, d3, gyroTime[i]);
        }

        if (checkSend && !isAlreadySend) {
            fuseTimer = new Timer();
            fuseTimer.scheduleAtFixedRate(new calculateFusedOrientationTask(getApplicationContext()), 0, TIME_CONSTANT);
            isAlreadySend = true;
        }
    }

    protected void sendToSensorFilter(float[] d1, float[] d2, float[] d3, long time)
    {
        sensorFilters.getSensor(d1, d2, d3, time);
    }


    @Override
    public void onPeerConnected(Node peer) {
        super.onPeerConnected(peer);

        Log.i(TagName.TAG, "Connected: " + peer.getDisplayName() + " (" + peer.getId() + ")");
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        super.onPeerDisconnected(peer);

        Log.i(TagName.TAG, "Disconnected: " + peer.getDisplayName() + " (" + peer.getId() + ")");
    }
}

