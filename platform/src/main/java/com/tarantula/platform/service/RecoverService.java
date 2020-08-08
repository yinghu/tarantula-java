package com.tarantula.platform.service;

import com.tarantula.platform.service.cluster.ReplicationData;

public interface RecoverService extends ServiceProvider{

    String NAME = "RecoverService";

    byte[] recover(String source,byte[] key);
    void replicate(String source,int partition,byte[] key,byte[] value);
    void batch(ReplicationData[] batch);

}
