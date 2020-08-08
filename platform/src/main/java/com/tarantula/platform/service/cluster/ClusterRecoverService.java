package com.tarantula.platform.service.cluster;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.spi.ManagedService;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.spi.RemoteService;
import com.tarantula.TarantulaLogger;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.TarantulaContext;

import java.util.Properties;

public class ClusterRecoverService implements ManagedService, RemoteService {
    private static TarantulaLogger log = JDKLogger.getLogger(ClusterRecoverService.class);

    private NodeEngine nodeEngine;
    private TarantulaContext tarantulaContext;
    @Override
    public void init(NodeEngine nodeEngine, Properties properties) {
        this.nodeEngine = nodeEngine;
        tarantulaContext = TarantulaContext.getInstance();
        log.warn("Cluster Recover Service Started");
    }

    @Override
    public void reset() {

    }

    @Override
    public void shutdown(boolean b) {

    }

    @Override
    public DistributedObject createDistributedObject(String s) {
        return new RecoverServiceProxy(s,nodeEngine,this);
    }

    @Override
    public void destroyDistributedObject(String s) {

    }
    public byte[] load(String source,byte[] key){
        log.warn("Recovering ["+new String(key)+"]from ["+source+"]");
        return this.tarantulaContext.dataStore(source,tarantulaContext.partitionNumber()).backup().get(key);
    }
    public void replicate(String source,int partition,byte[] key,byte[] value){
        log.warn("Replicating ["+new String(key)+"]from ["+source+"]["+partition+"]");
        this.tarantulaContext.dataStore(source,tarantulaContext.partitionNumber()).backup().set(key,value);
    }
    public void replicateAsBatch(ReplicationData[] batch){
        for(ReplicationData d : batch){
            replicate(d.source,d.partition,d.key,d.value);
        }
    }
}
