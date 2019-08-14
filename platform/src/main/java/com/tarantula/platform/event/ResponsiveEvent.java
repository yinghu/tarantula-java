package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.Event;
import com.tarantula.platform.Data;

import java.io.IOException;


public class ResponsiveEvent extends Data implements Event {


	public ResponsiveEvent(){}

	public ResponsiveEvent(String destination,String sessionId,byte[] payload,String label,boolean closed){
        this.destination = destination;
        this.sessionId = sessionId;
        this.payload = payload;
        this.label = label;
        this.closed = closed;
	}
    public ResponsiveEvent(String destination,String sessionId,byte[] payload,int batch,String contentType,String label,boolean closed){
        this.destination = destination;
        this.sessionId = sessionId;
        this.payload = payload;
        this.retries = batch;
        this.contentType = contentType;
        this.label = label;
        this.closed = closed;
    }
    @Override
    public int getClassId() {
        return PortableEventRegistry.RESPONSIVE_EVENT_CID;
    }
    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }
    @Override
    public void writePortable(PortableWriter out) throws IOException {
		out.writeUTF("1",this.destination);
        out.writeUTF("2",this.sessionId);
        out.writeByteArray("4",this.payload);
        out.writeUTF("5",this.contentType);
        out.writeUTF("6",this.label);
        out.writeBoolean("7",this.closed);
        out.writeInt("8",this.retries);
    }
    @Override
	public void readPortable(PortableReader in) throws IOException {
		this.destination = in.readUTF("1");
        this.sessionId = in.readUTF("2");
        this.payload = in.readByteArray("4");
        this.contentType = in.readUTF("5");
        this.label = in.readUTF("6");
        this.closed = in.readBoolean("7");
        this.retries = in.readInt("8");
	}
	@Override
	public String toString(){
		return "Responsive ["+this.sessionId+","+closed+","+retries+"]["+label+"]";
	}

}
