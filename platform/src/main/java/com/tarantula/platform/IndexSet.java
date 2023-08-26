package com.tarantula.platform;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.service.cluster.PortableRegistry;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class IndexSet extends RecoverableObject {

    protected Set<String> keySet = new HashSet<>();

    public IndexSet(){
    }
    public IndexSet(String label){
        this.label = label;
    }

    @Override
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

    @Override
    public int getClassId() {
        return PortableRegistry.INDEX_SET_CID;
    }

    @Override
    public Recoverable.Key key(){
        return new AssociateKey(this.id,this.label);
    }

    public boolean addKey(String key){
        boolean added;
        synchronized (keySet){
            added = keySet.add(key);
        }
        return added;
    }
    public boolean removeKey(String key){
        boolean removed;
        synchronized (keySet){
            removed = keySet.remove(key);
            properties.remove(key);
        }
        return removed;
    }
    public Set<String> keySet(){
        HashSet<String> copy = new HashSet();
        synchronized (keySet){
            copy.addAll(keySet);
        }
        return copy;
    }
    public void reload(){
        synchronized (keySet){
            keySet.clear();
        }
        this.dataStore.load(this);
    }
    public void clear(){
        synchronized (keySet){
            keySet.clear();
        }
        this.dataStore.update(this);
    }

}
