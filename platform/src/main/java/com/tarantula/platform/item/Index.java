package com.tarantula.platform.item;

import com.icodesoftware.Configurable;
import com.tarantula.platform.IndexSet;

import java.util.Map;

public class Index extends IndexSet implements Configurable {

    public Index(){

    }
    public Index(String query){
        this.label = "index/"+query;
    }

    @Override
    public int getFactoryId() {
        return ItemPortableRegistry.OID;
    }

    @Override
    public Map<String,Object> toMap(){
        super.toMap();
        return properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        super.fromMap(properties);
    }

    @Override
    public int getClassId() {
        return ItemPortableRegistry.INDEX_CID;
    }

    public void index(String key){
        keySet.add(key);
        this.dataStore.update(this);
    }
}