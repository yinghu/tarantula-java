package com.icodesoftware.service;

public interface BucketListener {
    //bucket number zero based
    //state open, close, shutdown
    int OPEN = 0;
    int CLOSE = 1;
    int SHUT_DOWN = 2;
    void onBucket(int bucket,int state);
}
