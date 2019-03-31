package org.anhcraft.youtubedownloader.utils;

import java.util.concurrent.TimeUnit;

public class SpeedClock {
    private long lastTime;
    private double oldTotalKB;
    private double kbPerMs;

    public void refresh(){
        lastTime = System.nanoTime();
        oldTotalKB = 0;
        kbPerMs = 0;
    }

    public double updateSpeed(double newTotalKB){
        long d = TimeUnit.MILLISECONDS.convert(System.nanoTime()-lastTime, TimeUnit.NANOSECONDS);
        d = d <= 0 ? 1 : d;
        kbPerMs = (newTotalKB-oldTotalKB)/d;
        lastTime = System.nanoTime();
        oldTotalKB = newTotalKB;
        return kbPerMs;
    }
}
