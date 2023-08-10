package com.tarantula.platform.service.cluster.keyindex;

import com.icodesoftware.service.ServiceProvider;


public interface DistributionKeyIndexService extends ServiceProvider {

    String NAME = "DistributionKeyIndexService";

    byte[] recover(int partition,byte[] key);

    boolean startSync(String syncKey);
    void onSync(int size,byte[][] keys,byte[][] values,String memberId,int partition);
    boolean endSync(String memberId,String syncKey);

}
