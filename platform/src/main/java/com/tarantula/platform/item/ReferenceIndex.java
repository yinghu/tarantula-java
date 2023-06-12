package com.tarantula.platform.item;

import com.icodesoftware.Configurable;
import com.icodesoftware.Recoverable;
import com.icodesoftware.util.NaturalKey;
import com.tarantula.platform.IndexSet;

import java.util.Map;

public class ReferenceIndex extends IndexSet implements Configurable {

    public ReferenceIndex(){
        this.label = "references";
    }
    public ReferenceIndex(String name){
        this();
        this.name = name;
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
        return ItemPortableRegistry.REFERENCE_INDEX_CID;
    }

    public Key key(){
        return new NaturalKey(name+ Recoverable.PATH_SEPARATOR+label);
    }
    public void index(String key){
        keySet.add(key);
    }
}