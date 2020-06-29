package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.Event;

import java.io.IOException;

/**
 * Created by yinghu lu on 6/28/2020
 */
public class MapStoreBackupEvent extends Data implements Event {

    public MapStoreBackupEvent(){}

    public MapStoreBackupEvent(String destination){
        this.destination = destination;
    }

    @Override
    public void writePortable(PortableWriter out) throws IOException {
        out.writeUTF("1",this.destination);
    }
    @Override
    public void readPortable(PortableReader in) throws IOException {
        this.destination = in.readUTF("1");
    }
    @Override
    public int getClassId() {
        return PortableEventRegistry.MAP_STORE_BACKUP_EVENT_CID;
    }
    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }
    public String toString(){
        return "MapStoreBackupEvent->"+destination;
    }
}
