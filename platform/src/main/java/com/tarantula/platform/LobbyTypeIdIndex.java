package com.tarantula.platform;

import com.tarantula.platform.service.cluster.PortableRegistry;

public class LobbyTypeIdIndex extends RecoverableObject {

    public int getFactoryId() {
        return PortableRegistry.OID;
    }

    public LobbyTypeIdIndex(){
        this.binary = true;
    }
    public LobbyTypeIdIndex(String bucket,String typeId){
        this();
        this.bucket = bucket;
        this.label =  typeId;
    }
    public LobbyTypeIdIndex(String bucket,String typeId,String index){
        this();
        this.bucket = bucket;
        this.label =  typeId;
        this.index = index;
    }
    @Override
    public byte[] toByteArray(){
        return this.index.getBytes();
    }
    @Override
    public void fromByteArray(byte[] data){
        this.index = new String(data);
    }

    public int getClassId() {
        return PortableRegistry.LOBBY_TYPE_ID_INDEX_CID;
    }
    @Override
    public Key key(){
        return new CompositeKey(this.bucket,label);
    }
}
