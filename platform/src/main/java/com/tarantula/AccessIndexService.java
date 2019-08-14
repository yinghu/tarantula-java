package com.tarantula;

import com.hazelcast.core.DistributedObject;

/**
 * Updated by yinghu lu on 7/11/2018.
 */
public interface AccessIndexService extends DistributedObject,ServiceProvider{

    String NAME = "AccessIndexService";

    AccessIndex set(String accessKey,String systemId);

    AccessIndex get(String accessKey);

}
