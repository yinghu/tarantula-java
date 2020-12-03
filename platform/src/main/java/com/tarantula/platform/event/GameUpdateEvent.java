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
    public GameUpdateEvent(String zoneId,String roomId,int type,byte[] payload){
        this.destination = zoneId;
        this.trackId = roomId;
        this.payload = payload;
        this.stub = type;
    }
    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeUTF("1",destination);
        portableWriter.writeUTF("2",trackId);
        portableWriter.writeByteArray("3",payload);
        portableWriter.writeInt("4",this.stub);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        this.destination = portableReader.readUTF("1");
        this.trackId = portableReader.readUTF("2");
        this.payload = portableReader.readByteArray("3");
        this.stub = portableReader.readInt("4");
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
