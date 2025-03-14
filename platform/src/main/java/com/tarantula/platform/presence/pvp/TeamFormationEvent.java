package com.tarantula.platform.presence.pvp;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Event;
import com.tarantula.platform.event.Data;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;

public class TeamFormationEvent extends Data implements Event {


    public TeamFormationEvent(long playerId,long teamId){
        this();
        this.stub = playerId;
        this.distributionId = teamId;
    }

    public TeamFormationEvent(){
        this.destination = GameEndEvent.GAME_END_TOPIC;
    }

    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeLong("1",distributionId);
        portableWriter.writeLong("2",stub);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        this.distributionId = portableReader.readLong("1");
        this.stub = portableReader.readLong("2");
    }

    @Override
    public int getClassId() {
        return PortableEventRegistry.TEAM_FORMATION_EVENT_CID;
    }
    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }
}
