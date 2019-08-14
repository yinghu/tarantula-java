package com.tarantula;

/**
 * Update by yinghu lu on 10/8/2018.
 */
public interface OnTimeout {
    long timeout();
    void timeout(long timeout);
}
