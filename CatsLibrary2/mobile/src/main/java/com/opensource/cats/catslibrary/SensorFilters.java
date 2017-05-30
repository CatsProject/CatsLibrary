package com.opensource.cats.catslibrary;

import android.hardware.SensorManager;

import static com.opensource.cats.catslibrary.Data.accMagOrientationM;
import static com.opensource.cats.catslibrary.Data.accMagOrientationW;
import static com.opensource.cats.catslibrary.Data.fusedSensorW;
import static com.opensource.cats.catslibrary.Data.gyroMatrixM;
import static com.opensource.cats.catslibrary.Data.gyroMatrixW;
import static com.opensource.cats.catslibrary.Data.gyroOrientationM;
import static com.opensource.cats.catslibrary.Data.gyroOrientationW;
import static com.opensource.cats.catslibrary.Data.isInput;
import static com.opensource.cats.catslibrary.Data.rotationMatrixM;
import static com.opensource.cats.catslibrary.Data.rotationMatrixW;

/**
 * Created by C on 2016-11-20.
 */

public class SensorFilters implements TagName {
    private boolean initState;
    private long timestamp;

    private int time = 0;

    //public static SensorFiltering instance;

    private float[] ac = new float[3];
    private float[] gy = new float[3];
    private float[] mg = new float[3];

    public SensorFilters(boolean isMobileOrWearable) {
        initState = true;

        if(isMobileOrWearable) {
            for(int i = 0; i < MATRIX_SIZE; i++) {
                gyroMatrixM[i] = 0.0f;

                if(i == 0 || i == 4 || i == 8) {
                    gyroMatrixM[i] = 1.0f;
                }
            }
            gyroOrientationM[0] = 0.0f;
            gyroOrientationM[1] = 0.0f;
            gyroOrientationM[2] = 0.0f;
        }
        else {
            for (int i = 0; i < MATRIX_SIZE; i++) {
                gyroMatrixW[i] = 0.0f;

                if (i == 0 || i == 4 || i == 8) {
                    gyroMatrixW[i] = 1.0f;
                }
            }
            gyroOrientationW[0] = 0.0f;
            gyroOrientationW[1] = 0.0f;
            gyroOrientationW[2] = 0.0f;
        }
    }

    public void stopTimer() {
    }


    public void getSensor(float[] sensorValues, int type, long timestamp) { // mobile
        switch (type) {
            case SENS_ACCELEROMETER:
                ac = sensorValues;
                calculateAccMagOrientation(true);
                break;
            case SENS_GYROSCOPE:
                gy = sensorValues;
                gyroFunctionM(timestamp,  sensorValues);
                break;
            case SENS_MAGNETIC:
                mg = sensorValues;
                break;
        }
    }

    public void getSensor(float[] ac, float[] gy, float[] mg, long timestamp){ //wearable

        this.ac = ac;
        this.gy = gy;
        this.mg = mg;
        calculateAccMagOrientation(false);
        gyroFunctionW(timestamp, this.gy);

        for (int i = 0; i < 3; i++) {
            if (gyroOrientationW[i] < -0.5 * Math.PI && accMagOrientationW[i] > 0.0) {
                fusedSensorW[time][i] = (float) (FILTER_COEFFICIENT * (gyroOrientationW[i] + 2.0 * Math.PI) + oneMinusCoeff * accMagOrientationW[i]);
                fusedSensorW[time][i] -= (fusedSensorW[time][i] > Math.PI) ? 2.0 * Math.PI : 0;
            } else if (accMagOrientationW[i] < -0.5 * Math.PI && gyroOrientationW[i] > 0.0) {
                fusedSensorW[time][i] = (float) (FILTER_COEFFICIENT * gyroOrientationW[i] + oneMinusCoeff * (accMagOrientationW[i] + 2.0 * Math.PI));
                fusedSensorW[time][i] -= (fusedSensorW[time][i] > Math.PI) ? 2.0 * Math.PI : 0;
            } else {
                fusedSensorW[time][i] = FILTER_COEFFICIENT * gyroOrientationW[i] + oneMinusCoeff * accMagOrientationW[i];
            }
        }
        gyroMatrixW = getRotationMatrixFromOrientation(fusedSensorW[time]);
        System.arraycopy(fusedSensorW[time], 0, gyroOrientationW, 0, 3);
        isInput = true;

        time++; if(time >= 30) time = 0;


    }


    public void calculateAccMagOrientation(boolean isMobileOrWearable) {
        if(isMobileOrWearable) {
            if (SensorManager.getRotationMatrix(rotationMatrixM, null, ac, mg)) {
                SensorManager.getOrientation(rotationMatrixM, accMagOrientationM);
            }
        }
        else {
            if (SensorManager.getRotationMatrix(rotationMatrixW, null, ac, mg)) {
                SensorManager.getOrientation(rotationMatrixW, accMagOrientationW);
            }
        }
    }

    public void gyroFunctionM(long time, float[] values) {
        if (accMagOrientationM == null)
            return;

        if (initState) {
            float[] initMatrix = new float[9];
            initMatrix = getRotationMatrixFromOrientation(accMagOrientationM);
            float[] test = new float[3];
            SensorManager.getOrientation(initMatrix, test);
            gyroMatrixM = matrixMultiplication(gyroMatrixM, initMatrix);

            //  System.arraycopy(temp, 0, gyroMatrix, 0, 4);
            initState = false;
        }

        // copy the new gyro values into the gyro array
        // convert the raw gyro data into a rotation vector
        float[] deltaVector = new float[4];
        if (timestamp != 0) {
            final float dT = (time - timestamp) * NS2S;
            System.arraycopy(values, 0, gy, 0, 3);
            getRotationVectorFromGyro(gy, deltaVector, dT / 2.0f);
        }

        timestamp = time;

        float[] deltaMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(deltaMatrix, deltaVector);

        gyroMatrixM = matrixMultiplication(gyroMatrixM, deltaMatrix);

        //System.arraycopy(temp, 0, gyroMatrix, 0, 4);
        // get the gyroscope based orientation from the rotation matrix
        SensorManager.getOrientation(gyroMatrixM, gyroOrientationM);
    }


    public void gyroFunctionW(long time, float[] values) {

        if (accMagOrientationW == null)
            return;

        if (initState) {
            float[] initMatrix = new float[9];
            initMatrix = getRotationMatrixFromOrientation(accMagOrientationW);
            float[] test = new float[3];
            SensorManager.getOrientation(initMatrix, test);
            gyroMatrixW = matrixMultiplication(gyroMatrixW, initMatrix);

            //  System.arraycopy(temp, 0, gyroMatrix, 0, 4);
            initState = false;
        }

        // copy the new gyro values into the gyro array
        // convert the raw gyro data into a rotation vector
        float[] deltaVector = new float[4];
        if (timestamp != 0) {
            final float dT = (time - timestamp) * NS2S;
            System.arraycopy(values, 0, gy, 0, 3);
            getRotationVectorFromGyro(gy, deltaVector, dT / 2.0f);
        }

        timestamp = time;

        float[] deltaMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(deltaMatrix, deltaVector);

        gyroMatrixW = matrixMultiplication(gyroMatrixW, deltaMatrix);

        //System.arraycopy(temp, 0, gyroMatrix, 0, 4);

        // get the gyroscope based orientation from the rotation matrix
        SensorManager.getOrientation(gyroMatrixW, gyroOrientationW);
    }

    private void getRotationVectorFromGyro(float[] gyroValues, float[] deltaRotationVector, float timeFactor) {
        float[] normValues = new float[3];

        // Calculate the angular speed of the sample

        float omegaMagnitude = (float) Math.sqrt(gyroValues[0] * gyroValues[0] + gyroValues[1] * gyroValues[1] + gyroValues[2] * gyroValues[2]);
        //float omegaMagnitude = (float) Math.sqrt(Math.pow(gyroValues[0], 2) + Math.pow(gyroValues[1], 2) + Math.pow(gyroValues[2], 2));

        // Normalize the rotation vector if it's big enough to get the axis

        if (omegaMagnitude > EPSILON) {
            for(int i = 0; i < 3; i++) {
                normValues[i] = gyroValues[i] / omegaMagnitude;
            }
        }

        float thetaOverTwo = omegaMagnitude * timeFactor;
        float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
        float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);

        for(int i = 0; i < 3; i++) {
            deltaRotationVector[i] = sinThetaOverTwo * normValues[i];
        }
        deltaRotationVector[3] = cosThetaOverTwo;
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
