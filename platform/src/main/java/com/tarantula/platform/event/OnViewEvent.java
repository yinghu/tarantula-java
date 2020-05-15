package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.Event;
import com.tarantula.platform.OnViewTrack;


import java.io.IOException;

public class OnViewEvent extends Data implements Event {

    public OnViewEvent(){}
    public OnViewEvent(OnViewTrack onViewTrack){
        this.portable = onViewTrack;
    }

    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
            
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {

    }
}
