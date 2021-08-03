package com.tarantula.platform.tournament;

import com.icodesoftware.Tournament;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.IndexSet;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TournamentHeader extends RecoverableObject implements Tournament {

    private static final String TOURNAMENT_REGISTER = "register";
    private static final String TOURNAMENT_PLAY = "play";

    protected String type;
    protected String description;
    protected String icon;
    protected Status status = Status.SCHEDULED;
    protected LocalDateTime startTime;
    protected LocalDateTime closeTime;
    protected LocalDateTime endTime;
    protected int maxEntriesPerInstance;
    protected int durationMinutes;

    public IndexSet tournamentRegisterIndex;
    public IndexSet tournamentPlayIndex;
    public ConcurrentHashMap<String,TournamentInstanceHeader> _instanceIndex;

    public TournamentHeader(Schedule schedule){
        this.type = schedule.type();
        this.description = schedule.description();
        this.icon = schedule.icon();
        this.startTime = schedule.startTime();
        this.closeTime = schedule.closeTime();
        this.endTime = schedule.endTime();
        this.maxEntriesPerInstance = schedule.maxEntriesPerInstance();
        this.durationMinutes = schedule.instanceDurationInMinutes();
    }

    public TournamentHeader(){

    }
    @Override
    public String type() {
        return type;
    }
    @Override
    public void type(String type){
        this.type = type;
    }
    @Override
    public String description() {
        return description;
    }
    @Override
    public String icon() {
        return icon;
    }

    @Override
    public Status status(){
        return status;
    }
    @Override
    public LocalDateTime startTime() {
        return startTime;
    }
    public Map<String,Object> toMap(){
        properties.put("1",type);
        properties.put("2", TimeUtil.toUTCMilliseconds(startTime));
        properties.put("3", TimeUtil.toUTCMilliseconds(closeTime));
        properties.put("4", TimeUtil.toUTCMilliseconds(endTime));
        properties.put("5",maxEntriesPerInstance);
        properties.put("6",durationMinutes);
        return properties;
    }
    public void fromMap(Map<String,Object> properties){
        this.type = (String)properties.get("1");
        this.startTime = TimeUtil.fromUTCMilliseconds(((Number)properties.get("2")).longValue());
        this.closeTime = TimeUtil.fromUTCMilliseconds(((Number)properties.get("3")).longValue());
        this.endTime = TimeUtil.fromUTCMilliseconds(((Number)properties.get("4")).longValue());
        this.maxEntriesPerInstance = ((Number)properties.get("5")).intValue();
        this.durationMinutes = ((Number)properties.get("6")).intValue();
    }
    @Override
    public LocalDateTime closeTime() {
        return closeTime;
    }

    @Override
    public LocalDateTime endTime() {
        return endTime;
    }
    public int maxEntriesPerInstance(){
        return maxEntriesPerInstance;
    }
    public int durationMinutesPerInstance(){
        return durationMinutes;
    }


    @Override
    public String register(String systemId) {
        TournamentRegistry tournamentRegistry = new TournamentRegistry(maxEntriesPerInstance);
        this.dataStore.create(tournamentRegistry);
        tournamentRegisterIndex.keySet.add(tournamentRegistry.distributionKey());
        tournamentRegisterIndex.update();
        tournamentRegistry.addPlayer(systemId);
        dataStore.update(tournamentRegistry);
        return tournamentRegistry.distributionKey();
    }
    public Tournament.Instance lookup(String instanceId){
        return _instanceIndex.computeIfAbsent(instanceId,(k)->{
            TournamentInstanceHeader instanceHeader = new TournamentInstanceHeader(maxEntriesPerInstance,startTime,closeTime,endTime);
            this.dataStore.create(instanceHeader);
            instanceHeader.dataStore(dataStore);
            tournamentPlayIndex.keySet.add(instanceHeader.distributionKey());
            tournamentPlayIndex.update();
            return instanceHeader;
        });
    }
    @Override
    public int getFactoryId() {
        return TournamentPortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return TournamentPortableRegistry.TOURNAMENT_CID;
    }
    public void setup(ConcurrentHashMap<String,TournamentInstanceHeader> instanceIndex){
        this._instanceIndex = instanceIndex;
        tournamentRegisterIndex = new IndexSet(TOURNAMENT_REGISTER);
        tournamentRegisterIndex.distributionKey(this.distributionKey());
        this.dataStore.createIfAbsent(tournamentRegisterIndex,true);
        this.tournamentRegisterIndex.dataStore(dataStore);

        tournamentPlayIndex = new IndexSet(TOURNAMENT_PLAY);
        tournamentPlayIndex.distributionKey(this.distributionKey());
        this.dataStore.createIfAbsent(tournamentPlayIndex,true);
        tournamentPlayIndex.dataStore(this.dataStore);
    }

}
