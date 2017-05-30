package com.opensource.cats.catslibrary;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

public class MobileActivity extends AppCompatActivity implements TagName{


    //when you communicate between mobile and wearable, using GoogleApiClient
    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile);


        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Log.i("안녕", "연결");
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                    }
                }).build();
        googleApiClient.connect();


        //웨어러블에게 sensor service 를 시작하라고 명령.
        new CatsSendWearable(START, TAG, googleApiClient).start();

        //웨어러블에게 Sensor service 를 중지하라고 보냄
        new CatsSendWearable(STOP, TAG, googleApiClient).start();


        // 휴대폰
        startService(new Intent(getApplicationContext(), CatsSensorServiceM.class));

        stopService(new Intent(getApplicationContext(), CatsSensorServiceM.class));



        //set filter coefficient to make complementary filter.
        CatsMobileController.setFilterCoefficient(0.95f);


        // set Left Thresholds of spread and bend
        CatsMobileController.setLeftThresholds(30, -10);
        CatsMobileController.setRightThresholds(30, -10);
        CatsMobileController.setUpThresholds(30, -10);
        CatsMobileController.setDownThresholds(30, -10);

        // Do not touch init version.
        /*
        CatsMobileController.initLeftMotionCheck();
        CatsMobileController.initRightMotionCheck();
        CatsMobileController.initUpMotionCheck();
        CatsMobileController.initDownMotionCheck();
        */

        //get left motion i = 0,1,2,3
        boolean leftMotion = CatsMobileController.isLeftMotion(3);
        boolean rightMotion = CatsMobileController.isRightMotion(3);
        boolean upMotion = CatsMobileController.isUpMotion(3);
        boolean downMotion = CatsMobileController.isDownMotion(3);

    }


    @Override
    protected void onStart() {
        if(!googleApiClient.isConnected())
            googleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        if(googleApiClient.isConnected())
            googleApiClient.disconnect();
        super.onStop();
    }
}