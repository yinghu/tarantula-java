package com.tarantula.platform.tournament;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;

import com.tarantula.platform.event.PortableEventRegistry;
import java.io.IOException;




public class TournamentRegisterStatus implements Portable {

    public int slot;
    public long instanceId;

    public TournamentRegisterStatus(){

    }

    public TournamentRegisterStatus(long instanceId, int slot){
        this.instanceId = instanceId;
        this.slot = slot;
    }

    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PortableEventRegistry.TOURNAMENT_REGISTER_STATUS_CID;
    }

    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeLong("1",instanceId);
        portableWriter.writeInt("2",slot);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        this.instanceId = portableReader.readLong("1");
        this.slot = portableReader.readInt("2");
    }


}
