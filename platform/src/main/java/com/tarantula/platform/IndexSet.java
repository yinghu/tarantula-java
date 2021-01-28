package com.tarantula.platform;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.service.cluster.PortableRegistry;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class IndexSet extends RecoverableObject {

    public Set<String> keySet = new HashSet<>();

    public IndexSet(){
    }

    public int getFactoryId() {
        return PortableRegistry.OID;
    }

    @Override
    public Map<String,Object> toMap(){
       keySet.forEach((k)->{
           properties.put(k,"1");
       });
       return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
       properties.forEach((k,v)->{
           keySet.add(k);
       });
    }

    public int getClassId() {
        return PortableRegistry.INDEX_SET_CID;
    }

    public Recoverable.Key key(){
        return new AssociateKey(this.bucket,this.oid,this.label);
    }

}
