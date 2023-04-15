package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Event;

import java.io.IOException;

public class FastPlayEvent extends Data implements Event {

    public FastPlayEvent(){

    }
    public FastPlayEvent(SessionForward forward){
        this();
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
        out.writeUTF("3",this.tournamentId);
        out.writeUTF("4",this.owner);
        //out.writeDouble("5",this.balance);
        out.writeInt("6",this.stub);
        out.writeUTF("7",this.ticket);
        out.writePortable("8",this.forward);
        out.writeInt("9",routingNumber);
        out.writeUTF("10",name);
        out.writeUTF("11",clientId);
        out.writeByteArray("12",payload!=null?payload:new byte[0]);
    }
    @Override
    public void readPortable(PortableReader in) throws IOException {
        this.systemId = in.readUTF("1");
        this.destination = in.readUTF("2");
        this.tournamentId = in.readUTF("3");
        this.owner = in.readUTF("4");
        //this.balance = in.readDouble("5");
        this.stub = in.readInt("6");
        this.ticket = in.readUTF("7");
        this.forward = in.readPortable("8");
        this.routingNumber = in.readInt("9");
        this.name = in.readUTF("10");
        this.clientId = in.readUTF("11");
        this.payload = in.readByteArray("12");
    }
    @Override
    public void write(byte[] payload){
        this.write(payload,true);
    }
    @Override
    public void write(byte[] payload,boolean closed){
        this.eventService.publish(new ResponsiveEvent(this.forward.source(),this.forward.sessionId(),payload,closed));
    }
    @Override
    public String toString(){
        return "FastPlayEvent ["+this.systemId+"/"+this.stub+"]";
    }

}
