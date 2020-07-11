package com.tarantula.platform.service.deployment;

import com.tarantula.Configuration;
import com.tarantula.Recoverable;
import com.tarantula.platform.DeploymentObject;
import com.tarantula.platform.service.cluster.PortableRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * Updated by yinghu on 7/10/2020
 * **/
public class ServiceConfiguration extends DeploymentObject {

    public static final String LABEL = "SCG";

    public ServiceConfiguration(){

        this.label = LABEL;
        this.onEdge = true;
    }
    public ServiceConfiguration(String tag,int priority){
        this();
        this.tag = tag;
        this.priority = priority;
    }
    public String tag;
    public String serviceProviderName;
    public int priority;

    public HashMap<Recoverable.Key,Configuration> configurationMappings = new HashMap<>();


    @Override
    public Map<String,Object> toMap(){
        this.properties.put("tag",tag);
        this.properties.put("serviceProviderName",serviceProviderName);
        this.properties.put("priority",this.priority);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.tag = (String)properties.get("tag");
        this.serviceProviderName = (String)properties.get("serviceProviderName");
        this.priority =((Number)properties.get("priority")).intValue();
    }


    @Override
    public int getFactoryId() {
        return PortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PortableRegistry.SERVICE_CONFIGURATION_CID;
    }
    @Override
    public String toString(){
        return "service configuration ["+tag+","+priority+","+serviceProviderName+"]";
    }
}
