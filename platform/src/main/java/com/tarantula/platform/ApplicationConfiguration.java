package com.tarantula.platform;
import com.icodesoftware.Configurable;
import com.icodesoftware.Configuration;
import com.icodesoftware.Distributable;
import com.icodesoftware.Property;

import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.service.RecoverService;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.platform.service.cluster.PortableRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import com.icodesoftware.util.RecoverableObject;

public class ApplicationConfiguration extends RecoverableObject implements Configuration {

    public static final String LABEL = "AFC";

    private CopyOnWriteArrayList<Configurable.Listener> _listeners = new CopyOnWriteArrayList<>();

    private String type;

    public ApplicationConfiguration(){
        this.onEdge = true;
    }

    public void configurationType(String type) {
        this.type = type;
        this.properties.put("type",type);
    }
    public void configurationName(String name) {
        this.name = name;
        this.properties.put("name",name);
    }
    public String configurationType(){
        return this.type;
    }
    public String configurationName(){
        return this.name;
    }

    public String label(){
        return LABEL;
    }

    public List<Property> properties(){
        ArrayList<Property> _alist = new ArrayList();
        properties.forEach((String k,Object v)->{
            if(!k.equals("type")&&!k.equals("name")){
                DistributedProperty _p = new DistributedProperty(k,v);
                _alist.add(_p);
            }
        });
        return _alist;
    }

    @Override
    public Map<String,Object> toMap(){
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.type = (String)properties.get("type");
        this.name = (String)properties.get("name");
        properties.forEach((String k,Object v)->{
            this.properties.put(k,v);
        });
    }

    public int getFactoryId() {
        return PortableRegistry.OID;
    }

    public int getClassId() {
        return PortableRegistry.APPLICATION_CONFIGURATION_CID;
    }

    @Override
    public void registerListener(Listener listener){
        this._listeners.add(listener);
    }
    @Override
    public void updated(ServiceContext serviceContext){
        RecoverService rsp = serviceContext.clusterProvider(Distributable.DATA_SCOPE).recoverService();
        byte[] _data = rsp.load(null,DeploymentServiceProvider.DEPLOY_DATA_STORE,this.distributionKey().getBytes());
        this.fromBinary((_data));
        this._listeners.forEach((a)->a.onUpdated(this));
    }
}
