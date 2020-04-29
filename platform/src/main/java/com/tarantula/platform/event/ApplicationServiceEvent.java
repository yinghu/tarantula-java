package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;

import java.io.IOException;


public class ApplicationServiceEvent extends Data implements EventOnAction {


	public ApplicationServiceEvent(){}

	public ApplicationServiceEvent(String source, String sessionId, String systemId, String applicationId, String tag, byte[] data){
		this.source = source;
		this.sessionId = sessionId;
		this.systemId = systemId;
        this.applicationId = applicationId;
        this.tag = tag;
        this.payload = data;
	}
	@Override
	public void writePortable(PortableWriter out) throws IOException {
		out.writeUTF("1",this.source);
		out.writeUTF("2",this.sessionId);
        out.writeUTF("3",this.systemId);
        out.writeUTF("4",this.applicationId);
		out.writeUTF("5",this.tag);
        out.writeByteArray("6",this.payload);
		out.writeUTF("7", this.clientId);
		out.writeInt("8", this.stub);
		out.writeUTF("9",this.action);
		out.writeUTF("10",this.ticket);
		out.writeUTF("11",this.trackId);
		out.writeInt("12",this.routingNumber);
		out.writeUTF("13",this.destination);
		out.writeBoolean("14",this.streaming);
	}
	@Override
	public void readPortable(PortableReader in) throws IOException {
		this.source = in.readUTF("1");
		this.sessionId = in.readUTF("2");
        this.systemId = in.readUTF("3");
        this.applicationId = in.readUTF("4");
		this.tag = in.readUTF("5");
		this.payload = in.readByteArray("6");
		this.clientId = in.readUTF("7");
		this.stub = in.readInt("8");
		this.action = in.readUTF("9");
		this.ticket = in.readUTF("10");
		this.trackId = in.readUTF("11");
		this.routingNumber = in.readInt("12");
		this.destination = in.readUTF("13");
		this.streaming = in.readBoolean("14");
	}
	@Override
	public int getClassId() {
		return PortableEventRegistry.APPLICATION_SERVICE_EVENT_CID;
	}
	@Override
	public int getFactoryId() {
		return PortableEventRegistry.OID;
	}

    @Override
	public String toString(){
		return "APPLICATION SERVICE EVENT ["+action+"]["+this.systemId+"/"+this.applicationId+"/"+this.tag+"/"+source+"/"+sessionId+"]";
	}
}
