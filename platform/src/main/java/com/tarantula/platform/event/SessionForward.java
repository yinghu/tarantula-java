package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import java.io.IOException;

/**
 * Updated by yinghu on 8/23/19.
 */
public class SessionForward extends Data {

    public SessionForward(){}

    public SessionForward(String source, String sessionId){
        this.source = source;
        this.sessionId = sessionId;
    }

    public String sessionId() {
        return this.sessionId;
    }

    public void sessionId(String sessionId) {
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
        out.writeUTF("5",this.sessionId);
    }

    public void readPortable(PortableReader in) throws IOException {

        this.source = in.readUTF("4");
        this.sessionId = in.readUTF("5");
    }
    @Override
    public String toString(){
        return "On Session ["+source+","+sessionId+"]";
    }
}
