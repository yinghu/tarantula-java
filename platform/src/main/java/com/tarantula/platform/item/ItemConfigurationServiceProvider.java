package com.tarantula.platform.item;

import com.icodesoftware.Configurable;
import com.icodesoftware.Configuration;
import com.icodesoftware.service.ConfigurationServiceProvider;
import com.tarantula.platform.service.deployment.TypedListener;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ItemConfigurationServiceProvider implements ConfigurationServiceProvider {

    private ConcurrentHashMap<String, TypedListener> rListeners = new ConcurrentHashMap<>();

    private final String name;
    public ItemConfigurationServiceProvider(String name){
        this.name = name;
    }
    @Override
    public <T extends Configurable> void register(T config) {
        rListeners.forEach((k,c)->{
            if(c.type==null||c.type.equals(config.configurationType())){
                c.listener.onCreated(config);
            }
        });
    }

    @Override
    public <T extends Configurable> void release(T t) {

    }

    @Override
    public void configure(String s) {

    }

    @Override
    public <T extends Configuration> List<T> configurations(String s) {
        return null;
    }
    @Override
    public String registerConfigurableListener(String type, Configurable.Listener listener) {
        String rid = UUID.randomUUID().toString();
        this.rListeners.put(rid,new TypedListener(type,listener));
        //logger.warn("Listener registered with ->"+type);
        return rid;
    }
    @Override
    public void unregisterConfigurableListener(String registryKey){
        TypedListener t = rListeners.remove(registryKey);
        //logger.warn("Listener removed with ->"+t.type);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }
}
