package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;

import java.io.IOException;


public class ServiceActionEvent extends Data implements EventOnAction {


	public ServiceActionEvent(){}

    public ServiceActionEvent(String source,String sessionId,byte[] data){
        this.source = source;
        this.sessionId = sessionId;
        this.payload = data;
    }

	@Override
	public void writePortable(PortableWriter out) throws IOException {
		out.writeUTF("1",this.source);
		out.writeUTF("2",this.sessionId);
        out.writeUTF("3",this.systemId);
        out.writeByteArray("6",this.payload);
		out.writeUTF("7", this.clientId);
		out.writeInt("8", this.stub);
		out.writeUTF("9",this.action);
		out.writeUTF("10",this.ticket);
		out.writeUTF("11",this.trackId);
		out.writeInt("12",this.routingNumber);
		out.writeUTF("13",this.destination);
		out.writeUTF("15",this.tournamentId);
		out.writeUTF("16",this.name);
		out.writeInt("17",this.accessMode);
	}
	@Override
	public void readPortable(PortableReader in) throws IOException {
		this.source = in.readUTF("1");
		this.sessionId = in.readUTF("2");
        this.systemId = in.readUTF("3");
		this.payload = in.readByteArray("6");
		this.clientId = in.readUTF("7");
		this.stub = in.readInt("8");
		this.action = in.readUTF("9");
		this.ticket = in.readUTF("10");
		this.trackId = in.readUTF("11");
		this.routingNumber = in.readInt("12");
		this.destination = in.readUTF("13");
		this.tournamentId = in.readUTF("15");
		this.name = in.readUTF("16");
		this.accessMode = in.readInt("17");
	}
	@Override
	public int getClassId() {
		return PortableEventRegistry.SERVICE_ACTION_EVENT_CID;
	}
	@Override
	public int getFactoryId() {
		return PortableEventRegistry.OID;
	}

    @Override
	public String toString(){
		return "SERVICE ACTION EVENT ["+action+"]["+this.systemId+"/"+this.stub+"]";
	}
}
