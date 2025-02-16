package com.tarantula.platform;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.protocol.presence.TRAccessIndex;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;


public class AccessIndexTrack extends TRAccessIndex implements  Portable {


    public AccessIndexTrack(){
    }
    public AccessIndexTrack(String owner,int referenceId,long distributionId){
        this.owner = owner;
        this.referenceId = referenceId;
        this.distributionId = distributionId;
    }

    public AccessIndexTrack(String owner){
        this();
        this.owner = owner;
    }

    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PortableEventRegistry.ACCESS_INDEX_CID;
    }

    @Override
    public void writePortable(PortableWriter out) throws IOException {
        out.writeUTF("1",this.owner);
        out.writeInt("2",this.referenceId);
        out.writeLong("3",distributionId);
    }

    @Override
    public void readPortable(PortableReader in) throws IOException {
        this.owner = in.readUTF("1");
        this.referenceId = in.readInt("2");
        this.distributionId = in.readLong("3");
    }

}
