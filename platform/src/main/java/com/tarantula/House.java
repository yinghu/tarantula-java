package com.tarantula;

/**
 * Updated by yinghu on 4/16/2018.
 */
public interface House extends Balance {

    boolean bankrupt();
    void  bank(boolean bank);
    boolean  bank();
}
