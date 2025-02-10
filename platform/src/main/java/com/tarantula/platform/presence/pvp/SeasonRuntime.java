package com.tarantula.platform.presence.pvp;

import com.tarantula.platform.item.ConfigurableObject;
import com.tarantula.platform.presence.PresencePortableRegistry;

import java.nio.ByteBuffer;
import java.util.concurrent.ScheduledFuture;

public class SeasonRuntime extends ConfigurableObject {

    public long seasonRotation;
    public long sequence;
    public long currentSeason;
    public long endTime;
    public boolean ended;
    public ScheduledFuture<?> scheduledFuture;

    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PresencePortableRegistry.SEASON_RUNTIME_CID;
    }

    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeLong(currentSeason);
        buffer.writeLong(endTime);
        buffer.writeLong(sequence);
        buffer.writeBoolean(ended);
        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        currentSeason = buffer.readLong();
        endTime = buffer.readLong();
        sequence = buffer.readLong();
        ended = buffer.readBoolean();
        return true;
    }

    @Override
    public byte[] toBinary() {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(currentSeason);
        buffer.putLong(endTime);
        buffer.putLong(sequence);
        buffer.flip();
        return buffer.array();
    }

    @Override
    public void fromBinary(byte[] payload) {
        ByteBuffer buffer = ByteBuffer.wrap(payload);
        currentSeason = buffer.getLong();
        endTime = buffer.getLong();
        sequence = buffer.getLong();
    }

    public void schedule(SeasonRuntime seasonRuntime){
        currentSeason = seasonRuntime.currentSeason;
        endTime = seasonRuntime.endTime;
        sequence = seasonRuntime.sequence;
    }
}
