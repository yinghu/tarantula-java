package com.tarantula.platform.presence.pvp;

import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.presence.PresencePortableRegistry;

public class TeamFormationIndex extends RecoverableObject {

    public long teamId;
    public int totalTeams;

    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PresencePortableRegistry.TEAM_FORMATION_INDEX_CID;
    }

    @Override
    public boolean read(DataBuffer buffer) {
       teamId = buffer.readLong();
       totalTeams = buffer.readInt();
       timestamp = buffer.readLong();
       return true;
    }

    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeLong(teamId);
        buffer.writeInt(totalTeams);
        buffer.writeLong(timestamp);
        return true;
    }

    public boolean expired(){
        return TimeUtil.expired(TimeUtil.fromUTCMilliseconds(timestamp));
    }

}
