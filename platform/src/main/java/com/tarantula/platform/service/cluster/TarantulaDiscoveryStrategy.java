package com.tarantula.platform.service.cluster;

import com.hazelcast.logging.ILogger;
import com.hazelcast.nio.Address;
import com.hazelcast.spi.discovery.AbstractDiscoveryStrategy;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.discovery.SimpleDiscoveryNode;
import com.hazelcast.spi.partitiongroup.PartitionGroupStrategy;
import com.tarantula.platform.TarantulaContext;

import java.net.InetAddress;
import java.util.*;

/**
 * Created by yinghu lu on 7/6/2018.
 */
public class TarantulaDiscoveryStrategy extends AbstractDiscoveryStrategy {

    private int port;
    private int scope;

    public TarantulaDiscoveryStrategy(ILogger logger, Map<String, Comparable> properties){
        super(logger,properties);
        port = (int)properties.get("tarantula-port");
        scope = (int)properties.get("tarantula-scope");
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public Iterable<DiscoveryNode> discoverNodes() {
        try{
            Collection<DiscoveryNode> nlist = new ArrayList<>();
            List<InetAddress> alist = TarantulaContext.memberDiscovery(this.scope).find();
            alist.forEach((a)->{
                Map<String,Object> props = new HashMap<>();
                props.put("hostname",a.getHostName());
                nlist.add(new SimpleDiscoveryNode(new Address(a,port),props));
            });
            return nlist;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public PartitionGroupStrategy getPartitionGroupStrategy() {
        return super.getPartitionGroupStrategy();
    }

    @Override
    public Map<String, Object> discoverLocalMetadata() {
        return super.discoverLocalMetadata();
    }
}
