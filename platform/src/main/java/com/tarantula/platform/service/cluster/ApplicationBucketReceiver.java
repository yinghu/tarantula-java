package com.tarantula.platform.service.cluster;

import com.tarantula.Event;
import com.tarantula.EventListener;
import com.tarantula.platform.service.BucketReceiver;
import com.tarantula.platform.service.BucketReceiverListener;

/**
 * Updated by yinghu lu on 6/3/2019.
 */
public class ApplicationBucketReceiver implements BucketReceiver{

    private final int partition;
    private final String topic;
    private final EventListener eventListener;

    private boolean opening;
    private final BucketReceiverListener bucketReceiverListener;
    public ApplicationBucketReceiver(String topic, int partition, EventListener eventListener, BucketReceiverListener bucketReceiverListener){
        this.topic = topic;
        this.partition = partition;
        this.eventListener = eventListener;
        this.bucketReceiverListener = bucketReceiverListener;

    }
    @Override
    public int partition(){
        return this.partition;
    }
    @Override
    public String bucket(){
        return this.topic;
    }

    @Override
    public boolean opening(){
        return opening;
    }

    @Override
    public void open(){
        this.opening = true;
        this.bucketReceiverListener.onBucketReceiver(BucketReceiver.OPEN,this);
    }
    @Override
    public void close(){
        opening = false;
        this.bucketReceiverListener.onBucketReceiver(BucketReceiver.CLOSE,this);
    }
    @Override
    public void shutdown(){
        this.bucketReceiverListener.onBucketReceiver(BucketReceiver.SHUT_DOWN,this);
    }

    @Override
    public boolean onEvent(Event event) {
        return this.eventListener.onEvent(event);

    }
}
