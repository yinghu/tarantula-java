package com.tarantula.platform;

import com.tarantula.platform.service.cluster.PortableRegistry;

import java.util.Map;

public class LobbyTypeIdIndex extends RecoverableObject {

    public int getFactoryId() {
        return PortableRegistry.OID;
    }

    public LobbyTypeIdIndex(){

    }
    public LobbyTypeIdIndex(String bucket,String typeId){
        this.bucket = bucket;
        this.label =  typeId;
    }
    public LobbyTypeIdIndex(String bucket,String typeId,String index,String owner){
        this();
        this.bucket = bucket;
        this.label =  typeId;
        this.index = index;
        this.owner = owner;//game cluster id
    }
    @Override
    public Map<String,Object> toMap(){
        this.properties.put("index",index);
        this.properties.put("owner",owner);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.index = (String)properties.get("index");
        this.owner = (String)properties.get("owner");
    }

    public int getClassId() {
        return PortableRegistry.LOBBY_TYPE_ID_INDEX_CID;
    }
    @Override
    public Key key(){
        return new CompositeKey(this.bucket,label);
    }
}
