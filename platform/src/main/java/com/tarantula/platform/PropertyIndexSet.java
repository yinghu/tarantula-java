package com.tarantula.platform;

import com.icodesoftware.Property;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.SimpleProperty;
import com.tarantula.platform.service.cluster.PortableRegistry;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PropertyIndexSet extends RecoverableObject {

    protected Set<Property> keySet = new HashSet<>();

    public PropertyIndexSet(){
    }
    public PropertyIndexSet(String label){
        this.label = label;
    }

    @Override
    public int getFactoryId() {
        return PortableRegistry.OID;
    }

    @Override
    public Map<String,Object> toMap(){
        keySet.forEach((k)->{
           properties.put(k.name(),k.value().toString());
        });
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.properties.putAll(properties);
        properties.forEach((k,v)->{
           keySet.add(new SimpleProperty(k,v));
       });
    }

    @Override
    public int getClassId() {
        return PortableRegistry.PROPERTY_INDEX_SET_CID;
    }

    @Override
    public Key key(){
        return new AssociateKey(this.distributionId,this.label);
    }

    public boolean addKey(Property key){
        boolean added;
        synchronized (keySet){
            added = keySet.add(key);
        }
        return added;
    }
    public boolean removeKey(Property key){
        boolean removed;
        synchronized (keySet){
            removed = keySet.remove(key);
            properties.remove(key.name());
        }
        return removed;
    }
    public Set<Property> keySet(){
        HashSet<Property> copy = new HashSet();
        synchronized (keySet){
            copy.addAll(keySet);
        }
        return copy;
    }

    public Object value(String key){
        synchronized (keySet){
            return properties.get(key);
        }
    }

    public void clear(){
        synchronized (keySet){
            keySet.clear();
            properties.clear();
        }
        this.dataStore.update(this);
    }

}
