package com.tarantula.platform;


import com.tarantula.*;
import com.tarantula.platform.service.cluster.PortableRegistry;
import com.tarantula.platform.util.SystemUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Updated by yinghu on 7/19/2020
 */
public class ApplicationConfiguration extends ConfigurableObject implements Configuration{

    private String tag;
    private String type;


    public ApplicationConfiguration(){
        this.onEdge = true;
    }

    public String tag() {
        return this.tag;
    }

    public void tag(String tag) {
        this.tag = tag;
        this.properties.put("tag",tag);
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
            if((!k.equals("tag"))&&(!k.equals("type"))){
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
        this.tag = (String)properties.get("tag");
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


}
