package com.tarantula.platform.tournament;

import com.icodesoftware.Tournament;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.OnApplicationHeader;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;


public class TournamentRegister extends OnApplicationHeader {

    private AtomicInteger totalJoined = new AtomicInteger(0);

    public TournamentRegister(){
        this.label = Tournament.REGISTER_LABEL;
        this.onEdge = true;
    }


    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeLong(tournamentId);
        buffer.writeLong(timestamp);
        buffer.writeInt(totalJoined.get());
        return true;
    }

    public boolean closed(){
        if(TimeUtil.expired(TimeUtil.fromUTCMilliseconds(timestamp))) return true;
        if(totalJoined.get()<=0) return true;
        return false;
    }
    @Override
    public boolean read(DataBuffer buffer) {
        tournamentId = buffer.readLong();
        timestamp = buffer.readLong();
        totalJoined.set(buffer.readInt());
        return true;
    }

    @Override
    public int getFactoryId() {
        return TournamentPortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return TournamentPortableRegistry.TOURNAMENT_REGISTER_CID;
    }

    public boolean available(){
        return totalJoined.decrementAndGet()>=0;
    }

    public void setup(long instanceId,int duration,int joins){
        this.tournamentId = instanceId;
        this.timestamp = TimeUtil.toUTCMilliseconds(LocalDateTime.now().minusMinutes(duration));
        this.totalJoined.set(joins);
    }

}
