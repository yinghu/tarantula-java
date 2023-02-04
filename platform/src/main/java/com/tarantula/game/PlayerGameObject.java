package com.tarantula.game;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Event;
import com.icodesoftware.Recoverable;
import com.tarantula.platform.event.Data;

import java.io.IOException;

public class PlayerGameObject extends Data implements Event {


    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {

    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {

    }

    @Override
    public String systemId(){
        return this.bucket+ Recoverable.PATH_SEPARATOR+oid;
    }
}
