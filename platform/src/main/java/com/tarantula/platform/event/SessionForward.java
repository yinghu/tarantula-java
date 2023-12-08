package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import java.io.IOException;

public class SessionForward extends Data {

    public SessionForward(){}

    public SessionForward(String source, long sessionId){
        this.source = source;
        this.sessionId = sessionId;
    }

    public String source() {
        return this.source;
    }

    public void source(String source) {
        this.source = source;
    }
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }

    public int getClassId() {
        return PortableEventRegistry.SINGLETON_FORWARD_CID;
    }

    public void writePortable(PortableWriter out) throws IOException {

        out.writeUTF("4",this.source);
        out.writeLong("5",this.sessionId);
    }

    public void readPortable(PortableReader in) throws IOException {

        this.source = in.readUTF("4");
        this.sessionId = in.readLong("5");
    }
    @Override
    public String toString(){
        return "On Session ["+source+","+sessionId+"]";
    }
}
