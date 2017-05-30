package com.opensource.cats.catslibrary;

/**
 * Created by C on 2017-05-29.
 */

public class    CatsMobileController {

    private static float filterCoefficient = 0.95f;
    private static double LeftMotionSpread  = 30;
    private static double LeftMotionBend  = -10;
    private static double RightMotionSpread  = 30;
    private static double RightMotionBend  = -10;
    private static double UpMotionSpread  = 30;
    private static double UpMotionBend  = -10;
    private static double DownMotionSpread  = 30;
    private static double DownMotionBend  = -10;

    private static boolean isLeftMotion[] = {false, false, false, false};
    private static boolean isRightMotion[] = {false, false, false, false};
    private static boolean isUpMotion[] = {false, false, false, false};
    private static boolean isDownMotion[] = {false, false, false, false};


    public static boolean isLeftMotion(int i )
    {
        return isLeftMotion[i];
    }
    public static boolean isRightMotion(int i )
    {
        return isRightMotion[i];
    }
    public static boolean isDownMotion(int i )
    {
        return isDownMotion[i];
    }
    public static boolean isUpMotion(int i )
    {
        return isUpMotion[i];
    }

    public static void setUpMotion(int i, boolean check)
    {
        isUpMotion[i] = check;
    }
    public static void setDownMotion(int i, boolean check)
    {
        isDownMotion[i] = check;
    }
    public static void setLeftMotion(int i, boolean check)
    {
        isLeftMotion[i] = check;
    }
    public static void setRightMotion(int i, boolean check)
    {
        isRightMotion[i] = check;
    }


    public static void initLeftMotionCheck(){
        for(int i = 0; i < 4; i++)
            isLeftMotion[i] = false;
    }
    public static void initRightMotionCheck(){
        for(int i = 0; i < 4; i++)
            isRightMotion[i] = false;
    }
    public static void initUpMotionCheck(){
        for(int i = 0; i < 4; i++)
            isUpMotion[i] = false;
    }
    public static void initDownMotionCheck(){
        for(int i = 0; i < 4; i++)
            isDownMotion[i] = false;
    }


    public static void setFilterCoefficient(float fc) {
        filterCoefficient = fc;
    }

    public static float getFilterCoefficient() {
        return filterCoefficient;
    }
    public static void setLeftThresholds(double spread , double bend) {
        LeftMotionSpread = spread;
        LeftMotionBend = bend;
    }

    public static void setRightThresholds(double spread , double bend) {
        RightMotionSpread = spread;
        RightMotionBend = bend;
    }

    public static void setUpThresholds(double spread , double bend) {
        UpMotionSpread = spread;
        UpMotionBend = bend;
    }
    public static void setDownThresholds(double spread , double bend) {
        DownMotionSpread = spread;
        DownMotionBend = bend;
    }


    public static double getLeftMotionSpread() {
        return LeftMotionSpread;
    }
    public static double getLeftMotionBend(){
        return LeftMotionBend;
    }

    public static double getRightMotionSpread() {
        return RightMotionSpread;
    }
    public static double getRightMotionBend(){
        return RightMotionBend;
    }

    public static double getUpMotionSpread() {
        return UpMotionSpread;
    }
    public static double getUpMotionBend(){
        return UpMotionBend;
    }

    public static double getDownMotionSpread() {
        return DownMotionSpread;
    }
    public static double getDownMotionBend(){
        return DownMotionBend;
    }

}
