package com.tarantula;

/**
 * Developer: YINGHU LU
 * Date updated: 5/3/2018
 * Time: 11:46 PM
 */

/**
 * Schedule task running on timer pool
 * */
public interface SchedulingTask extends Runnable {
    boolean oneTime();
    long initialDelay();
    long delay();
}
