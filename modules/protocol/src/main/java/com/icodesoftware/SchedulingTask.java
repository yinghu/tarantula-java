package com.icodesoftware;

public interface SchedulingTask extends Runnable {
    boolean oneTime();
    long initialDelay();
    long delay();
}
