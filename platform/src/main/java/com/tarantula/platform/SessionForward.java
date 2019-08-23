package com.tarantula.platform;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.platform.event.PortableEventRegistry;
import java.io.IOException;

/**
 * Updated by yinghu on 6/6/2018.
 */
public class SessionForward extends Data{

    private String applicationId;
    private String instanceId;
    private String source;
    private String sessionId;

    public SessionForward(){}

    public SessionForward(String source, String sessionId){
        this.source = source;
        this.sessionId = sessionId;
    }
    public String applicationId() {
        return applicationId;
    }


    public void applicationId(String applicationId) {
        this.applicationId =applicationId;
    }

    public String instanceId() {
        return this.instanceId;
    }

    public void instanceId(String instanceId) {
        this.instanceId = instanceId;
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

        out.writeUTF("2", this.applicationId);
        out.writeUTF("3",this.instanceId);
        out.writeUTF("4",this.source);
        out.writeUTF("5",this.sessionId);
    }

    public void readPortable(PortableReader in) throws IOException {
        this.applicationId = in.readUTF("2");
        this.instanceId = in.readUTF("3");
        this.source = in.readUTF("4");
        this.sessionId = in.readUTF("5");
    }
    @Override
    public String toString(){
        return "On Application ["+applicationId+","+instanceId+"] On Session ["+source+","+sessionId+"]";
    }
}
