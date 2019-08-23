package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.Event;
import com.tarantula.platform.Data;
import com.tarantula.platform.DeploymentDescriptor;

import java.io.IOException;

/**
 * Created by yinghu lu on 8/3/2019.
 */
public class ModuleResetEvent extends Data implements Event {


    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PortableEventRegistry.MODULE_RESET_EVENT_CID;
    }

    public ModuleResetEvent(){

    }
    public ModuleResetEvent(String destination, DeploymentDescriptor descriptor){
        this.destination = destination;
        this.portable = descriptor;
    }

    public void writePortable(PortableWriter out) throws IOException {
        out.writeUTF("1",this.destination);
        out.writePortable("2",this.portable);
    }

    public void readPortable(PortableReader in) throws IOException {
        this.destination = in.readUTF("1");
        this.portable = in.readPortable("2");
    }

    @Override
    public String toString(){
        return "Module Rest Event";
    }
}
