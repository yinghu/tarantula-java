package com.tarantula.test.datastore;

import com.hazelcast.client.config.ClientConfig;
import com.tarantula.platform.event.PortableEventRegistry;
import com.tarantula.platform.service.cluster.PortableRegistry;

import java.util.HashMap;

/**
 * Created by yinghu lu on 7/19/2018.
 */
public class DataStoreConfig {
    static HashMap<String,String> _cfg = new HashMap<>();
    static {
        _cfg.put("dir","/replication");
        _cfg.put("name","tarantula");
        _cfg.put("url","/data");
        _cfg.put("bucket","BDS01");
        _cfg.put("node","ND02");
        _cfg.put("bucketId","1");
        _cfg.put("fileIndex","10");
        _cfg.put("backup","true");
        _cfg.put("backupTopic","test");
    }
    static ClientConfig _dconfig = new ClientConfig();
    static{
        _dconfig.getProperties().setProperty("hazelcast.shutdownhook.enabled","true");
        _dconfig.getGroupConfig().setName("tarantula-dev-BDS01");
        _dconfig.getNetworkConfig().addAddress("10.0.0.234:5701");
        _dconfig.getSerializationConfig().addPortableFactory(PortableEventRegistry.OID,new PortableEventRegistry());
       // _dconfig.getSerializationConfig().addPortableFactory(PortableRegistry.OID,new PortableRegistry());
    }
    static ClientConfig _iconfig = new ClientConfig();
    static{
        _iconfig.getProperties().setProperty("hazelcast.shutdownhook.enabled","true");
        _iconfig.getGroupConfig().setName("tarantula-dev-integration");
        _iconfig.getNetworkConfig().addAddress("10.0.0.234:5702");
        _iconfig.getSerializationConfig().addPortableFactory(PortableEventRegistry.OID,new PortableEventRegistry());
       // _iconfig.getSerializationConfig().addPortableFactory(PortableRegistry.OID,new PortableRegistry());
    }
}
