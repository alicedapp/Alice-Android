package com.alice.model;

/**
 * @Description:
 * @Author: zhanghaoran3
 * @CreateDate: 2020/1/18 22:16
 */
public class GasPriceModel {
    public float fast;
    public float fastest;
    public float safeLow;
    public float average;
    public double block_time;
    public long blockNum;
    public double speed;
    public float safeLowWait;
    public float avgWait;
    public float fastWait;
    public float fastestWait;

    @Override
    public String toString() {
        return "GasPriceModel{" +
                "fast=" + fast +
                ", fastest=" + fastest +
                ", safeLow=" + safeLow +
                ", average=" + average +
                ", block_time=" + block_time +
                ", blockNum=" + blockNum +
                ", speed=" + speed +
                ", safeLowWait=" + safeLowWait +
                ", avgWait=" + avgWait +
                ", fastWait=" + fastWait +
                ", fastestWait=" + fastestWait +
                '}';
    }
}
