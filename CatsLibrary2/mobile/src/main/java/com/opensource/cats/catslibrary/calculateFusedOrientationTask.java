package com.opensource.cats.catslibrary;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.TimerTask;

import static com.opensource.cats.catslibrary.Data.accMagOrientationM;
import static com.opensource.cats.catslibrary.Data.differAll;
import static com.opensource.cats.catslibrary.Data.differM;
import static com.opensource.cats.catslibrary.Data.differW;
import static com.opensource.cats.catslibrary.Data.downStack;
import static com.opensource.cats.catslibrary.Data.fusedSensorM;
import static com.opensource.cats.catslibrary.Data.fusedSensorW;
import static com.opensource.cats.catslibrary.Data.gyroMatrixM;
import static com.opensource.cats.catslibrary.Data.gyroOrientationM;
import static com.opensource.cats.catslibrary.Data.isInput;
import static com.opensource.cats.catslibrary.Data.leftStack;
import static com.opensource.cats.catslibrary.Data.rightStack;
import static com.opensource.cats.catslibrary.Data.upStack;

/**
 * Created by C on 2017-05-29.
 */

public class calculateFusedOrientationTask extends TimerTask implements TagName {



    private int time = 0;

    private Context context;
    boolean startChecker = false;

    private float[] currentOriM = new float[3];
    private float[] lastOriM = new float[3];
    private float[] currentOriW = new float[3];
    private float[] lastOriW = new float[3];


    private int leftCount = 0;
    private int rightCount = 0;
    private int upCount = 0;
    private int downCount = 0;

    /*
    private boolean[] rightStack = {false, false, false, false};
    private boolean[] leftStack = {false, false, false, false};
    private boolean[] upStack = {false, false, false, false};
    private boolean[] downStack = {false, false, false, false};
    */

    private float xSum = 0;
    private float zSum = 0;

    private int leftRightTime = 5;
    private int upDownTime = 5;



    public calculateFusedOrientationTask(Context context) {
        this.context = context;
    }

    public void run() {
        if(!isInput)
            return;

        float oneMinusCoeff = 1.0f - FILTER_COEFFICIENT;
        for (int i = 0; i < 3; i++) {
            if (gyroOrientationM[i] < -0.5 * Math.PI && accMagOrientationM[i] > 0.0) {
                fusedSensorM[(time  + 6) % 30][i] = (float) (FILTER_COEFFICIENT * (gyroOrientationM[i] + 2.0 * Math.PI) + oneMinusCoeff * accMagOrientationM[i]);
                fusedSensorM[(time  + 6) % 30][i] -= (fusedSensorM[(time  + 6) % 30][i] > Math.PI) ? 2.0 * Math.PI : 0;
            } else if (accMagOrientationM[i] < -0.5 * Math.PI && gyroOrientationM[i] > 0.0) {
                fusedSensorM[(time  + 6) % 30][i] = (float) (FILTER_COEFFICIENT * gyroOrientationM[i] + oneMinusCoeff * (accMagOrientationM[i] + 2.0 * Math.PI));
                fusedSensorM[(time  + 6) % 30][i] -= (fusedSensorM[(time  + 6) % 30][i] > Math.PI) ? 2.0 * Math.PI : 0;
            } else {
                fusedSensorM[(time  + 6) % 30][i] = FILTER_COEFFICIENT * gyroOrientationM[i] + oneMinusCoeff * accMagOrientationM[i];
            }
        }
        Log.i("Timer2"," : "+time);
        gyroMatrixM = getRotationMatrixFromOrientation(fusedSensorM[(time  + 6) % 30]);
        System.arraycopy(fusedSensorM[(time  + 6) % 30], 0, gyroOrientationM, 0, 3);


        for (int i = 0; i < 3; i++) { //mobile
            //-180~180이던 값을 0~360으로 변환
            if (fusedSensorM[(time  + 6) % 30][i] * 180 / (float) Math.PI < 0)
                currentOriM[i] = 360 + (fusedSensorM[(time  + 6) % 30][i] * 180 / (float) Math.PI);
            else
                currentOriM[i] = (fusedSensorM[(time  + 6) % 30][i] * 180 / (float) Math.PI);
            // 0이하, 360이상에 대한 처리 & 이전값의 변화량 저장
            if ((currentOriM[i] - lastOriM[i]) > 180)
                differM[(time  + 6) % 30][i] = (currentOriM[i] - lastOriM[i]) - 360;
            else if ((currentOriM[i] - lastOriM[i]) < -180)
                differM[(time  + 6) % 30][i] = (currentOriM[i] - lastOriM[i]) + 360;
            else
                differM[(time  + 6) % 30][i] = currentOriM[i] - lastOriM[i];

            //ignore noises => 변화량이 2도 이하면 노이즈로 취급, 0을 저장
            if (Math.abs(differM[(time  + 6) % 30][i]) < 8)
                differM[(time  + 6) % 30][i] = 0;
            lastOriM[i] = currentOriM[i];
        }

        for (int i = 0; i < 3; i++) { //wear
            //-180~180이던 값을 0~360으로 변환
            if (fusedSensorW[time][i] * 180 / (float) Math.PI < 0)
                currentOriW[i] = 360 + (fusedSensorW[time][i] * 180 / (float) Math.PI);
            else
                currentOriW[i] = (fusedSensorW[time][i] * 180 / (float) Math.PI);
            // 0이하, 360이상에 대한 처리 & 이전값의 변화량 저장
            if ((currentOriW[i] - lastOriW[i]) > 180)
                differW[time][i] = (currentOriW[i] - lastOriW[i]) - 360;
            else if ((currentOriW[i] - lastOriW[i]) < -180)
                differW[time][i] = (currentOriW[i] - lastOriW[i]) + 360;
            else
                differW[time][i] = currentOriW[i] - lastOriW[i];

            //ignore noises => 변화량이 2도 이하면 노이즈로 취급, 0을 저장
            if (Math.abs(differW[time][i]) < 8)
                differW[time][i] = 0;
            lastOriW[i] = currentOriW[i];
        }
        Log.i("CurrentOr",""+currentOriW[0] + ": " +differW[time][0]);

        for (int i = 0; i < 3; i++) {
            differAll[time][i] = differW[time][i] - differM[(time  + 6) % 30][i];
        }
        xSum = xSum + differAll[time][0];
        zSum = zSum + differAll[time][2];

        // noise check
        if(differAll[time][0] > -5 && differW[time][0] < 5) leftRightTime--;
        if(differAll[time][2] > -5 && differW[time][2] < 5) upDownTime--;

        leftRightAlgorithm();
        upDownAlgorithm();
        showMonitor();



        time++;
        if (time >= 30) {
            time = 0;
        }
        sendData();
    }
    private void leftRightAlgorithm() {

        if(leftRightTime == 0) {
            initLR();
            return;
        }
        if(xSum < - CatsMobileController.getLeftMotionSpread() && !rightStack[0]) {
            if(!leftStack[0] || (leftStack[1] && !leftStack[2]))  setLeftRightStack(true);
        }
        else if(xSum > - CatsMobileController.getLeftMotionBend() && leftStack[0]) {
            if(!leftStack[1]) setLeftRightStack(true);
            if(leftStack[2]) {
                checkDirection(true, false, false, false);
                Log.i("showMonitor","< < < <");
                leftCount++;
                initUD();
                initLR();
            }
        }
        if(xSum > CatsMobileController.getRightMotionSpread() && !leftStack[0]) {
            if(!rightStack[0] || (rightStack[1] && !rightStack[2]))  setLeftRightStack(false);

        }
        else if(xSum < CatsMobileController.getRightMotionBend() && rightStack[0]) {
            if(!rightStack[1]) setLeftRightStack(false);
            if(rightStack[2]) {
                checkDirection(false, true, false, false);
                Log.i("showMonitor","> > > >");
                rightCount++;
                initUD();
                initLR();
            }
        }
    }
    private void upDownAlgorithm() {

        if(upDownTime == 0) {
            initUD();
            return;
        }
        if(zSum < -CatsMobileController.getUpMotionSpread() && !downStack[0]) {
            if(!upStack[0] || (upStack[1] && !upStack[2]))  setUpDownStack(true);
        }
        else if(zSum > - CatsMobileController.getUpMotionBend() && upStack[0]) {
            if(!upStack[1]) setUpDownStack(true);
            if(upStack[2]) {
                checkDirection(false, false, true, false);
                Log.i("showMonitor","up up up up");
                upCount++;
                initUD();
                initLR();
            }
        }
        if(zSum > CatsMobileController.getLeftMotionSpread() && !upStack[0]) {
            if(!downStack[0] || (downStack[1] && !downStack[2]))  setUpDownStack(false);

        }
        else if(xSum < CatsMobileController.getLeftMotionBend() && downStack[0]) {
            if(!downStack[1]) setUpDownStack(false);
            if(downStack[2]) {
                checkDirection(false, false, false, true);
                downCount++;
                Log.i("showMonitor","down down down down");
                initUD();
                initLR();
            }
        }
    }

    private void initLR() {
        for(int i = 0; i <  3; i++) {

            CatsMobileController.setLeftMotion(i, false);
            CatsMobileController.setRightMotion(i, false);
            leftStack[i] = false;
            rightStack[i] = false;
        }
        xSum = 0;
        leftRightTime =  5;
    }
    private void initUD() {
        for(int i = 0; i <  3; i++) {

            CatsMobileController.setUpMotion(i, false);
            CatsMobileController.setDownMotion(i, false);
            upStack[i] = false;
            downStack[i] = false;
        }
        zSum = 0;
        upDownTime =  5;
    }
    private void checkDirection(boolean left, boolean right, boolean up, boolean down) {
        if(leftStack[3] && left) {
            leftStack[3] = false;
            return ;
        }
        if(rightStack[3] && right) {
            rightStack[3] = false;
            return ;
        }
        if(upStack[3] && up) {
            upStack[3] = false;
            return ;
        }
        if(downStack[3] && down) {
            downStack[3] = false;
            return ;
        }
        leftStack[3] = left;
        rightStack[3] = right;
        upStack[3] = up;
        downStack[3] = down;
    }



    private void showMonitor() {
        for(int i = 2; i >= 0; i--) {
            if(leftStack[i]) {
                Log.i("showMonitor", "LeftStack: " + i + " ,Count: " + leftRightTime + " xSum: " +xSum);
                break;
            }
            else if(rightStack[i]){
                Log.i("showMonitor", "rightStack: " + i + " ,Count: " + leftRightTime + " xSum: " +xSum);
                break;
            }
            if(upStack[i]) {
                Log.i("showMonitor", "upStack: " + i + " ,Count: " + upDownTime + " zSum: " +zSum);
                break;
            }
            else if(downStack[i]){
                Log.i("showMonitor", "downStack: " + i + " ,Count: " + upDownTime + " xSum: " +zSum);
                break;
            }
        }

    }
    private void setLeftRightStack(boolean isLeftOrRight) {

        for(int i = 0; i < 3; i++) {
            if(isLeftOrRight && !leftStack[i]) {

                CatsMobileController.setLeftMotion(i, true);

                break;
            }
            else if (!isLeftOrRight && !rightStack[i]) {
                CatsMobileController.setRightMotion(i, true);

                break;
            }
        }
        leftRightTime = 5;
    }
    private void setUpDownStack(boolean isUpOrDown) {

        for(int i = 0; i < 3; i++) {
            if(isUpOrDown && !upStack[i]) {
                CatsMobileController.setUpMotion(i, true);
                break;
            }
            else if (!isUpOrDown && !downStack[i]) {
                CatsMobileController.setDownMotion(i, true);
                break;
            }
        }
        upDownTime = 5;
    }


    private void sendData() {
        Intent intent;
        intent = new Intent(MOBILE_BROADCAST);
        intent.putExtra("DataSend", "data");
        Bundle bundle = new Bundle();

        //Log.i("들어왔나요 모발", differ[0] +", " +differ[1]  + ", " + differ[2]);
        intent.putExtras(bundle);
        context.sendBroadcast(intent);
        //  LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private static float[] getRotationMatrixFromOrientation(float[] o) {
        float[] xM = new float[9];
        float[] yM = new float[9];
        float[] zM = new float[9];

        float sinX = (float) Math.sin(o[1]);
        float cosX = (float) Math.cos(o[1]);
        float sinY = (float) Math.sin(o[2]);
        float cosY = (float) Math.cos(o[2]);
        float sinZ = (float) Math.sin(o[0]);
        float cosZ = (float) Math.cos(o[0]);

        // rotation about x-axis (pitch)
        xM[0] = 1.0f;
        xM[1] = 0.0f;
        xM[2] = 0.0f;
        xM[3] = 0.0f;
        xM[4] = cosX;
        xM[5] = sinX;
        xM[6] = 0.0f;
        xM[7] = -sinX;
        xM[8] = cosX;

        // rotation about y-axis (roll)
        yM[0] = cosY;
        yM[1] = 0.0f;
        yM[2] = sinY;
        yM[3] = 0.0f;
        yM[4] = 1.0f;
        yM[5] = 0.0f;
        yM[6] = -sinY;
        yM[7] = 0.0f;
        yM[8] = cosY;

        // rotation about z-axis (azimuth)
        zM[0] = cosZ;
        zM[1] = sinZ;
        zM[2] = 0.0f;
        zM[3] = -sinZ;
        zM[4] = cosZ;
        zM[5] = 0.0f;
        zM[6] = 0.0f;
        zM[7] = 0.0f;
        zM[8] = 1.0f;

        // rotation order is y, x, z (roll, pitch, azimuth)
        float[] resultMatrix = matrixMultiplication(xM, yM);
        resultMatrix = matrixMultiplication(zM, resultMatrix); //????????????????왜?????????????
        return resultMatrix;
    }
    private static float[] matrixMultiplication(float[] A, float[] B) {
        float[] result = new float[9];

        result[0] = A[0] * B[0] + A[1] * B[3] + A[2] * B[6];
        result[1] = A[0] * B[1] + A[1] * B[4] + A[2] * B[7];
        result[2] = A[0] * B[2] + A[1] * B[5] + A[2] * B[8];

        result[3] = A[3] * B[0] + A[4] * B[3] + A[5] * B[6];
        result[4] = A[3] * B[1] + A[4] * B[4] + A[5] * B[7];
        result[5] = A[3] * B[2] + A[4] * B[5] + A[5] * B[8];

        result[6] = A[6] * B[0] + A[7] * B[3] + A[8] * B[6];
        result[7] = A[6] * B[1] + A[7] * B[4] + A[8] * B[7];
        result[8] = A[6] * B[2] + A[7] * B[5] + A[8] * B[8];

        return result;
    }
}
