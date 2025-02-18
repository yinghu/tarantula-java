package com.tarantula.platform.presence.pvp;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Event;
import com.icodesoftware.Rating;
import com.tarantula.game.GameRating;
import com.tarantula.platform.event.Data;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;

public class GameEndEvent extends Data implements Event {

    public static final String GAME_END_TOPIC = "pvp_battle_end_topic";

    public long defenseTeamId;
    public int defenseEloLevel;
    public long offenseTeamId;
    public int offenseEloLevel;

    public GameEndEvent(){
        this.destination = GAME_END_TOPIC;
    }

    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeLong("1",defenseTeamId);
        portableWriter.writeInt("2",defenseEloLevel);
        portableWriter.writeLong("3",offenseTeamId);
        portableWriter.writeInt("4",offenseEloLevel);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        defenseTeamId = portableReader.readLong("1");
        defenseEloLevel = portableReader.readInt("2");
        offenseTeamId = portableReader.readLong("3");
        offenseEloLevel = portableReader.readInt("4");
    }

    @Override
    public int getClassId() {
        return PortableEventRegistry.GAME_END_EVENT_CID;
    }
    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }
}
