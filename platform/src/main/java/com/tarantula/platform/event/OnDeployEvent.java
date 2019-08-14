package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.Event;
import com.tarantula.platform.Data;

import java.io.IOException;

/**
 * Updated by yinghu lu on 7/4/2018.
 */
public class OnDeployEvent extends Data implements Event {


    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PortableEventRegistry.ON_DEPLOY_EVENT_CID;
    }

    public OnDeployEvent(){

    }
    public OnDeployEvent(String instanceId){
        this.instanceId = instanceId;
    }

    public void writePortable(PortableWriter out) throws IOException {
        out.writeUTF("1",this.destination);
        out.writeUTF("2",this.source);
        out.writeUTF("3",this.instanceId);
    }

    public void readPortable(PortableReader in) throws IOException {
        this.destination = in.readUTF("1");
        this.source = in.readUTF("2");
        this.instanceId = in.readUTF("3");
    }

    @Override
    public String toString(){
        return "On Deploy ["+instanceId+"]";
    }
}
