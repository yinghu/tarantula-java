package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.Event;
import com.tarantula.platform.Data;

import java.io.IOException;

/**
 * Created by yinghu lu on 8/3/2019.
 */
public class ModuleLaunchEvent extends Data implements Event {


    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PortableEventRegistry.MODULE_LAUNCH_EVENT_CID;
    }

    public ModuleLaunchEvent(){

    }
    public ModuleLaunchEvent(String destination, String typeId){
        this.destination = destination;
        this.typeId = typeId;
    }

    public void writePortable(PortableWriter out) throws IOException {
        out.writeUTF("1",this.destination);
        out.writeUTF("2",this.typeId);

    }

    public void readPortable(PortableReader in) throws IOException {
        this.destination = in.readUTF("1");
        this.typeId = in.readUTF("2");
    }

    @Override
    public String toString(){
        return "Module launch Event ["+typeId+"]";
    }
}
