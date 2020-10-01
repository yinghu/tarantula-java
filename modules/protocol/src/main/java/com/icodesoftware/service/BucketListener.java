package com.icodesoftware.service;

/**
 * Updated by yinghu lu on 4/11/2019.
 */
public interface BucketListener {
    //bucket number zero based
    //state open, close, shutdown
    void onBucket(int bucket,int state);
}
