package com.tarantula.platform.service;

/**
 * Update by yinghu lu on 4/10/2019.
 * Callback on bucket receiver registered or unregistered
 */
public interface BucketReceiverListener {

    void onBucketReceiver(int state,BucketReceiver bucketReceiver);

}
