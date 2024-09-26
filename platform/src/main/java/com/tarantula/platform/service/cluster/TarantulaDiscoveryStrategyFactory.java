package com.tarantula.platform.service.cluster;

import com.hazelcast.config.properties.PropertyDefinition;
import com.hazelcast.config.properties.SimplePropertyDefinition;
import com.hazelcast.config.properties.PropertyTypeConverter;
import com.hazelcast.logging.ILogger;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.discovery.DiscoveryStrategy;
import com.hazelcast.spi.discovery.DiscoveryStrategyFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;


public class TarantulaDiscoveryStrategyFactory implements DiscoveryStrategyFactory {

    @Override
    public Class<? extends DiscoveryStrategy> getDiscoveryStrategyType() {
        return TarantulaDiscoveryStrategy.class;
    }

    @Override
    public DiscoveryStrategy newDiscoveryStrategy(DiscoveryNode discoveryNode, ILogger iLogger, Map<String, Comparable> map) {
        return new TarantulaDiscoveryStrategy(iLogger,map);
    }

    @Override
    public Collection<PropertyDefinition> getConfigurationProperties() {
        Collection<PropertyDefinition> clist =new ArrayList<>();
        clist.add(new SimplePropertyDefinition("tarantula-port",PropertyTypeConverter.INTEGER));
        return clist;
    }
}
