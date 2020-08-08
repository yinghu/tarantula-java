package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.Event;

import java.io.IOException;

/**
 * Updated by yinghu on 12/7/2019.
 */
public class TimeoutEvent extends Data implements Event {
    public TimeoutEvent(String systemId,int stub,int routingNumber){
        this.systemId = systemId;
        this.stub = stub;
        this.routingNumber =routingNumber;
    }

    @Override
    public String toString(){
        return "Time out ["+systemId+"/"+stub+"/"+routingNumber+"]";
    }

    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {

    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {

    }
}
