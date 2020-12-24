package com.tarantula.platform;
import com.icodesoftware.Configurable;
import com.icodesoftware.Configuration;
import com.icodesoftware.Distributable;
import com.icodesoftware.Property;
import com.icodesoftware.service.DeployService;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.service.RecoverService;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.platform.service.cluster.PortableRegistry;
import com.tarantula.platform.util.SystemUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Updated by yinghu on 7/19/2020
 */
public class ApplicationConfiguration extends RecoverableObject implements Configuration {

    private String type;
    private CopyOnWriteArrayList<Configurable.Listener> _listeners = new CopyOnWriteArrayList<>();

    public ApplicationConfiguration(){
        this.onEdge = true;
    }

    public String type() {
        return this.type;
    }

    public void type(String type) {
        this.type = type;
        this.properties.put("type",type);
    }

    public String label(){
        return Configuration.LABEL;
    }
    public void configure(String name,String value){
        this.properties.put(name,value);
    }
    public List<Property> properties(){
        ArrayList<Property> _alist = new ArrayList();
        properties.forEach((String k,Object v)->{
            if(!k.equals("type")){
                DistributedProperty _p = new DistributedProperty(k,v.toString());
                _alist.add(_p);
            }
        });
        return _alist;
    }
    public String property(String name){
        Object v = this.properties.get(name);
        return v!=null?v.toString():null;
    }

    @Override
    public Map<String,Object> toMap(){
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.type = (String)properties.get("type");
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

    public void registerListener(Listener listener){
        this._listeners.add(listener);
    }
    @Override
    public void update(ServiceContext serviceContext){
        RecoverService rsp = serviceContext.clusterProvider(Distributable.DATA_SCOPE).recoverService();
        byte[] _data = rsp.load(null,DeploymentServiceProvider.DEPLOY_DATA_STORE,this.distributionKey().getBytes());
        this.fromBinary((_data));
        this._listeners.forEach((a)->a.onUpdated(this));
    }
}
