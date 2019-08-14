package com.tarantula.platform.event;


import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.Event;
import com.tarantula.platform.Data;
import com.tarantula.platform.SessionForward;

import java.io.IOException;

/**
 * Updated by ying hu 4/8/2018.
 */
public class InstancePlayEvent extends Data implements Event {


    public InstancePlayEvent(){
        this.forwarding = true;
    }
    public InstancePlayEvent(String applicationId, String instanceId,SessionForward forward){
        this();
        this.applicationId = applicationId;
        this.instanceId = instanceId;
        this.forward = forward;
    }
    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PortableEventRegistry.INSTANCE_PLAY_EVENT_CID;
    }
    @Override
    public void writePortable(PortableWriter out) throws IOException {
        out.writeUTF("1",this.systemId);
        out.writeUTF("2",this.destination);
        out.writeUTF("5",this.applicationId);
        out.writeUTF("6",this.instanceId);
        out.writeUTF("7",this.owner);
        out.writeDouble("8",this.balance);
        out.writeInt("9",this.stub);
        out.writeInt("10",this.accessMode);
        out.writeUTF("11",this.ticket);
        out.writeUTF("12",this.trackId);
        out.writePortable("13",this.forward);
        out.writeInt("14",this.routingNumber);
    }
    @Override
    public void readPortable(PortableReader in) throws IOException {
        this.systemId = in.readUTF("1");
        this.destination = in.readUTF("2");
        this.applicationId = in.readUTF("5");
        this.instanceId = in.readUTF("6");
        this.owner = in.readUTF("7");
        this.balance = in.readDouble("8");
        this.stub = in.readInt("9");
        this.accessMode = in.readInt("10");
        this.ticket = in.readUTF("11");
        this.trackId = in.readUTF("12");
        this.forward = in.readPortable("13");
        this.routingNumber = in.readInt("14");
    }
    @Override
    public void write(byte[] payload,String label){
        this.write(payload,label,true);
    }

    @Override
    public void write(byte[] payload,String label,boolean closed){
        this.eventService.publish(new ResponsiveEvent(this.forward.source(),this.forward.sessionId(),payload,label,closed));
    }
    @Override
    public String toString(){
        return "On Game Listing Instance Event ["+this.systemId+","+this.stub+","+this.applicationId+","+this.instanceId+","+owner+","+accessMode+"]";//
    }
}
