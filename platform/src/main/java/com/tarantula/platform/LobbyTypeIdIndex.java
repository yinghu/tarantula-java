package com.tarantula.platform;

import com.tarantula.platform.service.cluster.PortableRegistry;

public class LobbyTypeIdIndex extends AssociateObject {

    private long lobbyId;
    private long gameClusterId;

    public LobbyTypeIdIndex(){

    }
    //for query
    public LobbyTypeIdIndex(long bucketId,String typeId){
        this.distributionId = bucketId;
        this.label =  typeId;
    }
    //for create
    public LobbyTypeIdIndex(long bucketId,String typeId,long lobbyId){
        this.distributionId = bucketId;
        this.label =  typeId;
        this.lobbyId = lobbyId;
    }
    public LobbyTypeIdIndex(long bucketId,String typeId,long lobbyId,long gameClusterId){
        this.distributionId = bucketId;
        this.label =  typeId;
        this.lobbyId = lobbyId;
        this.gameClusterId = gameClusterId;//game cluster id
    }

    public long lobbyId(){
        return lobbyId;
    }

    public long gameClusterId(){
        return gameClusterId;
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
