package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Event;

import java.io.IOException;

/**
 * updated by yinghu on 8/8/2020
 */
public class FastPlayEvent extends Data implements Event {

    public FastPlayEvent(){

    }
    public FastPlayEvent(String systemId,int stub, SessionForward forward){
        this();
        this.systemId = systemId;
        this.stub = stub;
        this.forward = forward;
    }
    public FastPlayEvent(String applicationId, SessionForward forward){
        this();
        this.applicationId = applicationId;
        this.forward = forward;
    }
    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PortableEventRegistry.FAST_PLAY_EVENT_CID;
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
        out.writePortable("14",this.forward);
        out.writeInt("15",routingNumber);
        out.writeByteArray("16",payload!=null?payload:new byte[0]);
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
        this.forward = in.readPortable("14");
        this.routingNumber = in.readInt("15");
        this.payload = in.readByteArray("16");
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
        return "Fast Play Event ["+this.systemId+","+this.stub+","+this.applicationId+","+this.instanceId+","+owner+","+accessMode+"]";//
    }

}
