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

    public long defensePlayerId;
    public long defenseTeamId;
    public int defenseEloLevel;
    public int defenseEloLevelDelta;

    public long offensePlayerId;
    public long offenseTeamId;
    public int offenseEloLevel;
    public int offenseEloLevelDelta;

    public GameEndEvent(){
        this.destination = GAME_END_TOPIC;
    }

    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeLong("1",defensePlayerId);
        portableWriter.writeLong("2",defenseTeamId);
        portableWriter.writeInt("3",defenseEloLevel);
        portableWriter.writeInt("4",defenseEloLevelDelta);
        portableWriter.writeLong("5",offensePlayerId);
        portableWriter.writeLong("6",offenseTeamId);
        portableWriter.writeInt("7",offenseEloLevel);
        portableWriter.writeInt("8",offenseEloLevelDelta);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        defensePlayerId = portableReader.readLong("1");
        defenseTeamId = portableReader.readLong("2");
        defenseEloLevel = portableReader.readInt("3");
        defenseEloLevelDelta = portableReader.readInt("4");
        offensePlayerId = portableReader.readLong("5");
        offenseTeamId = portableReader.readLong("6");
        offenseEloLevel = portableReader.readInt("7");
        offenseEloLevelDelta = portableReader.readInt("8");
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
