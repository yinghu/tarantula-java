package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Event;

import java.io.IOException;

public class GameClusterSyncEvent extends Data implements Event {



    public GameClusterSyncEvent(){

    }
    public GameClusterSyncEvent(String typeId,String query,byte[] payload){
        this.typeId = typeId;
        this.name = query;
        this.payload = payload;
    }
    @Override
    public void writePortable(PortableWriter out) throws IOException {
        out.writeUTF("1",this.destination);
        out.writeUTF("2",typeId);
        out.writeUTF("3",name);
        out.writeByteArray("4",payload);
    }
    @Override
    public void readPortable(PortableReader in) throws IOException {
        this.destination = in.readUTF("1");
        this.typeId = in.readUTF("2");
        this.name = in.readUTF("3");
        this.payload = in.readByteArray("4");
    }
    @Override
    public int getClassId() {
        return PortableEventRegistry.GAME_CLUSTER_SYNC_EVENT_CID;
    }
    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }
    @Override
    public String toString(){
        return "Game cluster sync event";
    }
}
