package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Event;

import java.io.IOException;

/**
 * Created by yinghu lu on 11/15/2020.
 */
public class GameUpdateEvent extends Data implements Event {

    public GameUpdateEvent(){}
    public GameUpdateEvent(String zoneId,String roomId){
        this.destination = zoneId;
        this.trackId = roomId;
    }
    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeUTF("1",destination);
        portableWriter.writeUTF("2",trackId);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        this.destination = portableReader.readUTF("1");
        this.trackId = portableReader.readUTF("2");
    }
    @Override
    public int getFactoryId(){
        return PortableEventRegistry.OID;
    }
    @Override
    public int getClassId(){
        return PortableEventRegistry.GAME_UPDATE_EVENT_CID;
    }
    public String toString(){
        return "GameUpdateEvent->"+destination+"<><><>"+trackId;
    }
}
