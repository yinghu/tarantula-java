package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.Event;

import java.io.IOException;

/**
 * Created by yinghu lu on 8/10/2019.
 */
public class ModuleApplicationEvent extends Data implements Event {


    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PortableEventRegistry.MODULE_APPLICATION_EVENT_CID;
    }

    public ModuleApplicationEvent(){

    }
    public ModuleApplicationEvent(String destination, String typeId,String applicationId,boolean disabled){
        this.destination = destination;
        this.typeId = typeId;
        this.applicationId = applicationId;
        this.disabled = disabled;
    }

    public void writePortable(PortableWriter out) throws IOException {
        out.writeUTF("1",this.destination);
        out.writeUTF("2",this.typeId);
        out.writeUTF("3",this.applicationId);
        out.writeBoolean("4",this.disabled);
    }

    public void readPortable(PortableReader in) throws IOException {
        this.destination = in.readUTF("1");
        this.typeId = in.readUTF("2");
        this.applicationId = in.readUTF("3");
        this.disabled = in.readBoolean("4");
    }

    @Override
    public String toString(){
        return "Module Application Event ["+typeId+","+applicationId+","+disabled+"]";
    }
}
