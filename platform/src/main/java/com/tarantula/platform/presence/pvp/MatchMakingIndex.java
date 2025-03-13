package com.tarantula.platform.presence.pvp;

import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.presence.PresencePortableRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public class MatchMakingIndex extends RecoverableObject {

    private final ArrayBlockingQueue<DefenseTeamIndex> higher2 = new ArrayBlockingQueue<>(2);

    private final ArrayBlockingQueue<DefenseTeamIndex> lower3 = new ArrayBlockingQueue<>(3);

    public final long[] teamIdBelow = new long[]{0,0,0};
    public final long[] teamIdHigh = new long[]{0,0};

    public int poolIndex;

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
        buffer.writeInt(poolIndex);
        for(int i=0; i<teamIdBelow.length;i++){
            buffer.writeLong(teamIdBelow[i]);
        }
        for(int i=0; i<teamIdHigh.length;i++){
            buffer.writeLong(teamIdHigh[i]);
        }
        buffer.writeLong(timestamp);
        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        poolIndex = buffer.readInt();
        for(int i=0; i<teamIdBelow.length;i++){
            teamIdBelow[i] = buffer.readLong();
        }
        for(int i=0; i<teamIdHigh.length;i++){
            teamIdHigh[i] = buffer.readLong();
        }
        timestamp = buffer.readLong();
        return true;
    }

    public void reset(){
        for(int i=0; i<teamIdBelow.length;i++){
            teamIdBelow[i] = 0;
        }
        for(int i=0; i<teamIdHigh.length;i++){
            teamIdHigh[i] = 0;
        }
        ArrayList<DefenseTeamIndex> pending = new ArrayList<>();
        lower3.drainTo(pending);
        for(int i=0; i<pending.size();i++){
            teamIdBelow[i] = pending.get(i).teamId();
        }
        pending.clear();
        higher2.drainTo(pending);
        for(int i=0; i<pending.size();i++){
            teamIdHigh[i] = pending.get(i).teamId();
        }
    }

    public boolean full(){
        return higher2.size() == 2 && lower3.size() == 3;
    }

    public boolean higher(DefenseTeamIndex defenseTeamIndex){
        higher2.offer(defenseTeamIndex);
        return full();
    }

    public boolean lower(DefenseTeamIndex defenseTeamIndex){
        lower3.offer(defenseTeamIndex);
        return full();
    }

    public List<DefenseTeamIndex> list(){
        ArrayList list = new ArrayList();
        for(int i=0; i<teamIdBelow.length;i++){
            if(teamIdBelow[i] > 0) list.add(new DefenseTeamIndex(teamIdBelow[i]));
        }
        for(int i=0; i<teamIdHigh.length;i++){
            if(teamIdHigh[i] > 0) list.add(new DefenseTeamIndex(teamIdHigh[i]));
        }
        return list;
    }
    public boolean expired(){
        return TimeUtil.expired(TimeUtil.fromUTCMilliseconds(timestamp));
    }

}
