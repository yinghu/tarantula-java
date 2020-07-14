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
        String[] src = source.split("_");
        return this.tarantulaContext.dataStore(src[0],tarantulaContext.partitionNumber()).get(key);
    }
    public void replicate(String source,byte[] key,byte[] value){
        log.warn("Replicating ["+new String(key)+"]from ["+source+"]");
        String[] src = source.split("_");
        this.tarantulaContext.dataStore(src[0],tarantulaContext.partitionNumber()).set(key,value);
    }
}
