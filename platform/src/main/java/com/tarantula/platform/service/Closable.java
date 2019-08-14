package com.tarantula.platform.service;

/**
 * Updated by yinghu on 6/29/2018.
 *
 * Close runnable gracefully
 */

public interface Closable extends Runnable {
    void close();
}
