package com.tarantula.platform.presence.pvp;

import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.presence.PresencePortableRegistry;

public class MatchMakingIndex extends RecoverableObject {

    public long teamIdBelow1;
    public long teamIdBelow2;
    public long teamIdBelow3;
    public long teamIdUp1;
    public long teamIdUp2;

    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PresencePortableRegistry.MATCH_MAKING_INDEX_CID;
    }

    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeLong(teamIdBelow1);
        buffer.writeLong(teamIdBelow2);
        buffer.writeLong(teamIdBelow3);
        buffer.writeLong(teamIdUp1);
        buffer.writeLong(teamIdUp2);
        buffer.writeLong(timestamp);
        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        teamIdBelow1 = buffer.readLong();
        teamIdBelow2 = buffer.readLong();
        teamIdBelow3 = buffer.readLong();
        teamIdUp1 = buffer.readLong();
        teamIdUp2 = buffer.readLong();
        timestamp = buffer.readLong();
        return true;
    }

    public boolean expired(){
        return TimeUtil.expired(TimeUtil.fromUTCMilliseconds(timestamp));
    }

}
