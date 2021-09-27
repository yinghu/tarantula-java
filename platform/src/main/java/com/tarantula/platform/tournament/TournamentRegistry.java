package com.tarantula.platform.tournament;

import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.RoomRegistry;

import java.time.LocalDateTime;
import java.util.Map;

public class TournamentRegistry extends RoomRegistry {

    private LocalDateTime closeTime;

    public TournamentRegistry(){
        super();
    }
    public TournamentRegistry(int maxSize){
        super(maxSize);
    }
    public TournamentRegistry(int maxSize, LocalDateTime closeTime){
        super(maxSize);
        this.closeTime = closeTime;
    }

    @Override
    public Map<String,Object> toMap(){
        super.toMap();
        properties.put("closeTime", TimeUtil.toUTCMilliseconds(closeTime));
        return properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.closeTime = TimeUtil.fromUTCMilliseconds(((Number)properties.remove("closeTime")).longValue());
        super.fromMap(properties);
    }
    public int getFactoryId() {
        return TournamentPortableRegistry.OID;
    }
    public int getClassId() {
        return TournamentPortableRegistry.TOURNAMENT_REGISTRY_CID;
    }
    LocalDateTime closeTime(){
        return closeTime;
    }
    boolean expired(){
        return TimeUtil.expired(closeTime);
    }

}
