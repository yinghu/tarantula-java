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

    public Rating offenseRating;
    public Rating defenseRating;

    public GameEndEvent(){
        this.destination = GAME_END_TOPIC;
    }

    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeLong("1",defenseRating.distributionId());
        portableWriter.writeInt("2",defenseRating.level());
        portableWriter.writeLong("3",offenseRating.distributionId());
        portableWriter.writeLong("4",defenseRating.level());
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        defenseRating = GameRating.from(portableReader.readLong("1"),portableReader.readInt("2"));
        defenseRating = GameRating.from(portableReader.readLong("3"),portableReader.readInt("4"));
    }

    @Override
    public int getClassId() {
        return PortableEventRegistry.SERVER_PUSH_EVENT_CID;
    }
    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }
}
