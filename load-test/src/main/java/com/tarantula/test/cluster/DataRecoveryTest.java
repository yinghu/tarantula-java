package com.tarantula.test.cluster;

import com.hazelcast.client.config.ClientConfig;

import com.tarantula.platform.event.PortableEventRegistry;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

public class DataRecoveryTest {
    static ClientConfig _dconfig = new ClientConfig();
    static{
        _dconfig.getProperties().setProperty("hazelcast.shutdownhook.enabled","true");
        _dconfig.getGroupConfig().setName("tarantula-dev-BDS01");
        _dconfig.getNetworkConfig().addAddress("10.0.0.234:5701");
        _dconfig.getSerializationConfig().addPortableFactory(PortableEventRegistry.OID,new PortableEventRegistry());
        //_dconfig.getSerializationConfig().addPortableFactory(PortableRegistry.OID,new PortableRegistry());
    }
    static ClientConfig _iconfig = new ClientConfig();
    static{
        _iconfig.getProperties().setProperty("hazelcast.shutdownhook.enabled","true");
        _iconfig.getGroupConfig().setName("tarantula-dev-integration");
        _iconfig.getNetworkConfig().addAddress("10.0.0.234:5702");
        _iconfig.getSerializationConfig().addPortableFactory(PortableEventRegistry.OID,new PortableEventRegistry());
        //_iconfig.getSerializationConfig().addPortableFactory(PortableRegistry.OID,new PortableRegistry());
    }
    public static void main(String[] args) throws Exception{
        ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(3);
        AtomicBoolean a = new AtomicBoolean(false);
        AtomicBoolean b = new AtomicBoolean(false);
        //ClusterRecoveryService crs = new ClusterRecoveryService(_dconfig,_iconfig,"/mnt",scheduledExecutorService,a,b);
       // crs.start();
        scheduledExecutorService.shutdown();
    }
}
