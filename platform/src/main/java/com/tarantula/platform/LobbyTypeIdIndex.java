package com.tarantula.platform;

import com.tarantula.platform.service.cluster.PortableRegistry;
import java.util.Map;

public class LobbyTypeIdIndex extends AssociateObject {

    public long lobbyId;
    public long gameClusterId;

    public LobbyTypeIdIndex(){

    }
    //for query
    public LobbyTypeIdIndex(long bucketId,String typeId){
        this.id = bucketId;
        this.label =  typeId;
    }
    //for create
    public LobbyTypeIdIndex(long bucketId,String typeId,long lobbyId,long gameClusterId){
        this.id = bucketId;
        this.label =  typeId;
        this.lobbyId = lobbyId;
        this.gameClusterId = gameClusterId;//game cluster id
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

    public boolean write(DataBuffer buffer){
        buffer.writeLong(lobbyId);
        buffer.writeLong(gameClusterId);
        return true;
    }
    public boolean read(DataBuffer buffer) {
        this.lobbyId = buffer.readLong();
        this.gameClusterId = buffer.readLong();
        return true;
    }
    public int getFactoryId() {
        return PortableRegistry.OID;
    }

    public int getClassId() {
        return PortableRegistry.LOBBY_TYPE_ID_INDEX_CID;
    }

}
