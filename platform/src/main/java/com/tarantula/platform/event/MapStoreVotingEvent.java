package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.Event;
import com.tarantula.platform.Data;

import java.io.IOException;

/**
 * Created by yinghu lu on 4/15/2019.
 */
public class MapStoreVotingEvent extends Data implements Event {

    public MapStoreVotingEvent(){}

    public MapStoreVotingEvent(String destination, String source,String registerId,int scope){
        this.destination = destination;
        this.source = source;
        this.trackId = registerId;
        this.stub = scope;
    }

    @Override
    public void writePortable(PortableWriter out) throws IOException {
        out.writeUTF("1",this.destination);
        out.writeUTF("2",this.source);
        out.writeUTF("3",this.trackId);
        out.writeInt("4",this.stub);
    }
    @Override
    public void readPortable(PortableReader in) throws IOException {
        this.destination = in.readUTF("1");
        this.source = in.readUTF("2");
        this.trackId = in.readUTF("3");
        this.stub = in.readInt("4");
    }
    @Override
    public int getClassId() {
        return PortableEventRegistry.MAP_STORE_VOTING_EVENT_CID;
    }
    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }
    public String toString(){
        return "MapStoreVotingEvent->"+destination+"/"+source+"/"+trackId+"/"+stub+"]";
    }
}
