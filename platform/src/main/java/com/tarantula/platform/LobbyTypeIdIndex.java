package com.tarantula.platform;

import com.tarantula.platform.service.cluster.PortableRegistry;

public class LobbyTypeIdIndex extends AssociateObject {


    public LobbyTypeIdIndex(){

    }
    //for query
    public LobbyTypeIdIndex(long bucketId,String typeId){
        this.distributionId = bucketId;
        this.label =  typeId;
    }
    //for create
    public LobbyTypeIdIndex(long bucketId,String typeId,String lobbyId){
        this.distributionId = bucketId;
        this.label =  typeId;
        this.owner = lobbyId;
    }
    public LobbyTypeIdIndex(long bucketId,String typeId,String lobbyId,String gameClusterId){
        this.distributionId = bucketId;
        this.label =  typeId;
        this.owner = lobbyId;
        this.index = gameClusterId;//game cluster id
    }


    public boolean write(DataBuffer buffer){
        buffer.writeUTF8(owner);
        buffer.writeUTF8(index);
        return true;
    }
    public boolean read(DataBuffer buffer) {
        this.owner = buffer.readUTF8();
        this.index = buffer.readUTF8();
        return true;
    }
    public int getFactoryId() {
        return PortableRegistry.OID;
    }

    public int getClassId() {
        return PortableRegistry.LOBBY_TYPE_ID_INDEX_CID;
    }

}
