package com.tarantula.platform.service;

import com.tarantula.Connection;
import com.tarantula.EventListener;
import com.tarantula.Serviceable;

/**
 * Created by yinghu lu on 7/14/2018.
 */
public interface Instance extends EventListener,Serviceable,BucketReceiverListener{

    String applicationId();
    int partition();
    String routingKey();
    void onPartition(String instanceId);

}
