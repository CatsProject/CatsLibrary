package com.opensource.cats.catslibrary;

/**
 * Created by C on 2017-05-29.
 */

public class CatsWearableData {
    public static float[] accData = new float[15];
    public static float[] gyroData = new float[15];
    public static float[] magnetData = new float[15];

    public static long[] gyroTimeStamp = new long[5];

    private static double weightAcc = 1.0;
    private static double weightGyro = 1.0;
    private static double weightMag = 1.0;


    private static double threshold = 10;
    private static int thresholdTime = 20;

    public static void setWeightSensors(double acc, double gyro, double mag)
    {
        weightAcc = acc;
        weightGyro = gyro;
        weightMag = mag;
    }

    public static void setThresholdTime(int tt) {
        thresholdTime = tt;
    }
    public static void setEchoThreshold(double et){
        threshold = et;
    }
    public static double getEchoThreshold() {
        return threshold;
    }
    public static double getWeightAcc(){
        return weightAcc;
    }
    public static double getWeightGyro(){
        return weightGyro;
    }
    public static double getWeightMag(){
        return weightMag;
    }
    public static int getThresholdTime(){
        return thresholdTime;
    }
}
