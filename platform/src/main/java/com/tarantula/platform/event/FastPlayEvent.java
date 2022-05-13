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
        out.writeUTF("6",this.tournamentId);
        out.writeUTF("7",this.owner);
        out.writeDouble("8",this.balance);
        out.writeInt("9",this.stub);
        out.writeInt("10",this.accessMode);
        out.writeUTF("11",this.ticket);
        out.writePortable("14",this.forward);
        out.writeInt("15",routingNumber);
        out.writeUTF("16",name);
        out.writeUTF("17",clientId);
        out.writeByteArray("18",payload!=null?payload:new byte[0]);
    }
    @Override
    public void readPortable(PortableReader in) throws IOException {
        this.systemId = in.readUTF("1");
        this.destination = in.readUTF("2");
        this.tournamentId = in.readUTF("6");
        this.owner = in.readUTF("7");
        this.balance = in.readDouble("8");
        this.stub = in.readInt("9");
        this.accessMode = in.readInt("10");
        this.ticket = in.readUTF("11");
        this.forward = in.readPortable("14");
        this.routingNumber = in.readInt("15");
        this.name = in.readUTF("16");
        this.clientId = in.readUTF("17");
        this.payload = in.readByteArray("18");
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
        return this.systemId+"/"+this.stub;
    }

}
