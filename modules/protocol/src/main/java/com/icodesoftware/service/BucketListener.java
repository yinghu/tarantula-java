package com.icodesoftware.service;

public interface BucketListener {
    //bucket number zero based
    //state open, close, shutdown
    void onBucket(int bucket,int state);
}
