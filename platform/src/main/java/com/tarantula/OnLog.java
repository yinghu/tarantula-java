package com.tarantula;



/**
 * Updated by yinghu on 6/29/2018.
 */
public interface OnLog extends Recoverable,OnHeader{

    int DEBUG = 1;
    int INFO = 2;
    int WARN = 3;
    int ERROR = 4;
}
