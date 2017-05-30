package com.opensource.cats.catslibrary;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.widget.TextView;

import static com.opensource.cats.catslibrary.CatsWearableData.setEchoThreshold;
import static com.opensource.cats.catslibrary.CatsWearableData.setThresholdTime;
import static com.opensource.cats.catslibrary.CatsWearableData.setWeightSensors;

public class WearableActivity extends Activity {

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wearable);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
            }
        });

        //Weight Sensor acc, gyro, mag  that make threshold
        setWeightSensors(0.2,  0.6,  0.2);

        // set threshold that is not
        setEchoThreshold(30.5);

        // set time that ignore sensors if these sensors  continue to fail threshold
        setThresholdTime(30);

    }
}
