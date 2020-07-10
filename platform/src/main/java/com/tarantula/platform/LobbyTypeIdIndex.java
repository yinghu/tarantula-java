package com.tarantula.platform;

import com.tarantula.platform.service.cluster.PortableRegistry;

import java.util.Map;

public class LobbyTypeIdIndex extends RecoverableObject {

    public int getFactoryId() {
        return PortableRegistry.OID;
    }

    public LobbyTypeIdIndex(){

    }
    //for query
    public LobbyTypeIdIndex(String bucketId,String typeId){
        this.bucket = bucketId;
        this.label =  typeId;
    }
    //for create
    public LobbyTypeIdIndex(String bucketId,String typeId,String index,String owner){
        this.bucket = bucketId;
        this.label =  typeId;
        this.index = index;
        this.owner = owner;//game cluster id
    }
    @Override
    public Map<String,Object> toMap(){
        this.properties.put("index",index);//lobby id
        this.properties.put("owner",owner);//game cluster id
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
