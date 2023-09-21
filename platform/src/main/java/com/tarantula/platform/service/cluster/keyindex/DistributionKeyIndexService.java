package com.tarantula.platform.service.cluster.keyindex;

import com.icodesoftware.service.DataStoreSummary;
import com.icodesoftware.service.ServiceProvider;


public interface DistributionKeyIndexService extends ServiceProvider {

    String NAME = "DistributionKeyIndexService";

    byte[] recover(String source,byte[] key);

    boolean startSync(String syncKey);
    void onSync(int size,byte[][] keys,byte[][] values,String memberId,String source);
    boolean endSync(String memberId,String syncKey);

    void load(String source,byte[] key,DataStoreSummary.View view);
}
