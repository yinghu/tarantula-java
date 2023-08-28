package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;

import java.io.IOException;


public class PermissionCheckEvent extends Data implements EventOnAction {


	public PermissionCheckEvent(){}

    public PermissionCheckEvent(String source, String sessionId){
        this.source = source;
        this.sessionId = sessionId;
    }

	@Override
	public void writePortable(PortableWriter out) throws IOException {
		out.writeUTF("1",this.source);
		out.writeUTF("2",this.sessionId);
        out.writeLong("3",this.id);
		out.writeUTF("4",this.token);
        out.writeInt("5", this.stub);
		out.writeUTF("6",this.action);
		out.writeUTF("7",this.ticket);
		out.writeInt("8",this.routingNumber);
		out.writeUTF("9",this.destination);
	}
	@Override
	public void readPortable(PortableReader in) throws IOException {
		this.source = in.readUTF("1");
		this.sessionId = in.readUTF("2");
        this.id = in.readLong("3");
		this.token = in.readUTF("4");
		this.stub = in.readInt("5");
		this.action = in.readUTF("6");
		this.ticket = in.readUTF("7");
		this.routingNumber = in.readInt("8");
		this.destination = in.readUTF("9");
	}
	@Override
	public int getClassId() {
		return PortableEventRegistry.PERMISSION_CHECK_EVENT_CID;
	}
	@Override
	public int getFactoryId() {
		return PortableEventRegistry.OID;
	}

    @Override
	public String toString(){
		return "SERVICE ACTION EVENT ["+action+"]["+this.id+"/"+this.stub+"]";
	}
}
