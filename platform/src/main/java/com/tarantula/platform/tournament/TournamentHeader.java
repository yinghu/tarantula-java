package com.tarantula.platform.tournament;


import com.google.gson.JsonObject;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Tournament;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.IndexSet;
import com.tarantula.platform.RoomRegistry;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ScheduledFuture;

public class TournamentHeader extends RecoverableObject implements Tournament, Portable {

    private static final String TOURNAMENT_REGISTER = "register";
    private static final String TOURNAMENT_PLAY = "play";
    private static final int END_BUFFER_MINUTES = 3;

    protected int schedule;
    protected String type;
    protected String description;
    protected double enterCost;
    protected Status status = Status.STARTED;
    protected LocalDateTime startTime;
    protected LocalDateTime closeTime;
    protected LocalDateTime endTime;
    protected int maxEntriesPerInstance;
    protected int durationMinutes;

    public IndexSet tournamentRegisterIndex;
    public IndexSet tournamentPlayIndex;
    private ConcurrentHashMap<String,TournamentInstanceHeader> _instanceIndex;
    private ConcurrentLinkedDeque<TournamentRegistry> pendingRegistryQueue;

    private PlatformTournamentServiceProvider tournamentServiceProvider;

    public ScheduledFuture<?> pendingSchedule;

    public TournamentHeader(TournamentSchedule schedule){
        this.schedule = schedule.schedule();
        this.type = schedule.type();
        this.name = schedule.name();
        this.description = schedule.description();
        if(schedule.schedule()==Tournament.DAILY_SCHEDULE){
            this.startTime = LocalDateTime.now();
            this.endTime = this.startTime.plusHours(24);
            this.closeTime = this.endTime.minusMinutes(schedule.durationMinutesPerInstance());
        }
        else if(schedule.schedule()==Tournament.WEEKLY_SCHEDULE){
            this.startTime = LocalDateTime.now();
            this.endTime = this.startTime.plusDays(7);
            this.closeTime = this.endTime.minusMinutes(schedule.durationMinutesPerInstance());
        }
        else if(schedule.schedule()==Tournament.MONTHLY_SCHEDULE){
            this.startTime = LocalDateTime.now();
            this.endTime = this.startTime.plusDays(30);
            this.closeTime = this.endTime.minusMinutes(schedule.durationMinutesPerInstance());
        }
        else {
            this.startTime = LocalDateTime.now();
            this.endTime = this.startTime.plusHours(schedule.durationHoursPerSchedule());
            this.closeTime = this.endTime.minusMinutes(schedule.durationMinutesPerInstance());
        }
        this.maxEntriesPerInstance = schedule.maxEntriesPerInstance();
        this.durationMinutes = schedule.durationMinutesPerInstance();
        this.enterCost = schedule.enterCost();
        this.index = schedule.distributionKey();
    }

    public TournamentHeader(){

    }
    public int schedule(){
        return this.schedule;
    }
    @Override
    public String type() {
        return type;
    }


    public String description() {
        return description;
    }

    public  double enterCost(){
        return enterCost;
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
        properties.put("1",schedule);
        properties.put("2",status.name());
        properties.put("3",type);
        properties.put("4",name);
        properties.put("5", TimeUtil.toUTCMilliseconds(startTime));
        properties.put("6", TimeUtil.toUTCMilliseconds(closeTime));
        properties.put("7", TimeUtil.toUTCMilliseconds(endTime));
        properties.put("8",maxEntriesPerInstance);
        properties.put("9",durationMinutes);
        properties.put("10",enterCost);
        properties.put("11",index);
        return properties;
    }
    public void fromMap(Map<String,Object> properties){
        this.schedule = ((Number)properties.get("1")).intValue();
        this.status = Status.valueOf((String)properties.get("2"));
        this.type = (String)properties.get("3");
        this.name = (String)properties.get("4");
        this.startTime = TimeUtil.fromUTCMilliseconds(((Number)properties.get("5")).longValue());
        this.closeTime = TimeUtil.fromUTCMilliseconds(((Number)properties.get("6")).longValue());
        this.endTime = TimeUtil.fromUTCMilliseconds(((Number)properties.get("7")).longValue());
        this.maxEntriesPerInstance = ((Number)properties.get("8")).intValue();
        this.durationMinutes = ((Number)properties.get("9")).intValue();
        this.enterCost = ((Number)properties.get("10")).intValue();
        this.index = (String)properties.get("11");
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
        if(status != Status.STARTED) return null;
        TournamentRegistry tournamentRegistry = pendingRegistryQueue.poll();
        if(tournamentRegistry==null){
            LocalDateTime closeTime = LocalDateTime.now().plusMinutes(this.durationMinutesPerInstance()-END_BUFFER_MINUTES);
            tournamentRegistry = new TournamentRegistry(maxEntriesPerInstance,closeTime);
            this.dataStore.create(tournamentRegistry);
            tournamentRegisterIndex.addKey(tournamentRegistry.distributionKey());
            tournamentRegisterIndex.update();
            this.tournamentServiceProvider.monitorRegistry(this,tournamentRegistry);
        }
        int joinStatus = tournamentRegistry.register(systemId);
        dataStore.update(tournamentRegistry);
        if(joinStatus == RoomRegistry.NOT_JOINED){
            return null; //caller to retry
        }
        if(joinStatus == RoomRegistry.FULLY_JOINED){
            return tournamentRegistry.instanceId();
        }
        pendingRegistryQueue.offer(tournamentRegistry);
        return tournamentRegistry.instanceId();
    }
    public Tournament.Instance lookup(String instanceId){
        return _instanceIndex.computeIfAbsent(instanceId,(k)->{
            LocalDateTime _startTime = LocalDateTime.now();
            LocalDateTime _closeTime = _startTime.plusMinutes(durationMinutes-END_BUFFER_MINUTES);
            LocalDateTime _endTime = _startTime.plusMinutes(durationMinutes);
            TournamentInstanceHeader instanceHeader = new TournamentInstanceHeader(maxEntriesPerInstance,_startTime,_closeTime,_endTime);
            instanceHeader.distributionKey(instanceId);
            this.dataStore.create(instanceHeader);
            instanceHeader.dataStore(dataStore);
            tournamentPlayIndex.addKey(instanceHeader.distributionKey());
            tournamentPlayIndex.update();
            this.tournamentServiceProvider.monitorInstanceOnClose(this,instanceHeader);
            return instanceHeader;
        });
    }
    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PortableEventRegistry.TOURNAMENT_CID;
    }

    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeUTF("1",type);
        portableWriter.writeUTF("2",name);
        portableWriter.writeLong("4",TimeUtil.toUTCMilliseconds(startTime));
        portableWriter.writeLong("5",TimeUtil.toUTCMilliseconds(closeTime));
        portableWriter.writeLong("6",TimeUtil.toUTCMilliseconds(endTime));
        portableWriter.writeInt("7",durationMinutes);
        portableWriter.writeUTF("8",bucket);
        portableWriter.writeUTF("9",oid);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        this.type = portableReader.readUTF("1");
        this.name = portableReader.readUTF("2");
        this.startTime = TimeUtil.fromUTCMilliseconds(portableReader.readLong("4"));
        this.closeTime = TimeUtil.fromUTCMilliseconds(portableReader.readLong("5"));
        this.endTime = TimeUtil.fromUTCMilliseconds(portableReader.readLong("6"));
        this.durationMinutes = portableReader.readInt("7");
        this.bucket = portableReader.readUTF("8");
        this.oid = portableReader.readUTF("9");
    }

    public void setup(ConcurrentHashMap<String,TournamentInstanceHeader> instanceIndex, PlatformTournamentServiceProvider tournamentServiceProvider){
        tournamentServiceProvider.log(toString());
        this._instanceIndex = instanceIndex;
        this.tournamentServiceProvider = tournamentServiceProvider;
        this.pendingRegistryQueue = new ConcurrentLinkedDeque();
        tournamentRegisterIndex = new IndexSet(TOURNAMENT_REGISTER);
        tournamentRegisterIndex.distributionKey(this.distributionKey());
        this.dataStore.createIfAbsent(tournamentRegisterIndex,true);
        this.tournamentRegisterIndex.dataStore(dataStore);
        this.tournamentRegisterIndex.keySet().forEach((k)->{
            TournamentRegistry tournamentRegistry = new TournamentRegistry(this.maxEntriesPerInstance);
            tournamentRegistry.distributionKey(k);
            if(this.dataStore.load(tournamentRegistry)){
                if(!tournamentRegistry.fullJoined()&&!tournamentRegistry.expired()){
                    this.tournamentServiceProvider.monitorRegistry(this,tournamentRegistry);
                    pendingRegistryQueue.offer(tournamentRegistry);
                }
                else{
                    tournamentRegisterIndex.removeKey(tournamentRegistry.distributionKey());
                    tournamentRegisterIndex.update();
                }
            }
        });
        tournamentPlayIndex = new IndexSet(TOURNAMENT_PLAY);
        tournamentPlayIndex.distributionKey(this.distributionKey());
        this.dataStore.createIfAbsent(tournamentPlayIndex,true);
        tournamentPlayIndex.dataStore(this.dataStore);
        this.tournamentPlayIndex.keySet().forEach((k)->{
            TournamentInstanceHeader instanceHeader = new TournamentInstanceHeader();
            instanceHeader.distributionKey(k);
            if(this.dataStore.load(instanceHeader)){
                instanceHeader.dataStore(dataStore);
                instanceHeader.load();
                _instanceIndex.put(k,instanceHeader);
                if(!TimeUtil.expired(instanceHeader.closeTime())){
                    this.tournamentServiceProvider.monitorInstanceOnClose(this,instanceHeader);
                }
                else{
                    this.tournamentServiceProvider.log("Expired tournament instance scheduled to end->"+instanceHeader.distributionKey());
                    this.tournamentServiceProvider.monitorInstanceOnEnd(this,instanceHeader);
                }
            }
        });
        status = Status.STARTED;
        this.dataStore.update(this);
    }

    void tournamentRegistryClosed(TournamentRegistry closed){
        //remove closed register
        this.pendingRegistryQueue.remove(closed);
        tournamentRegisterIndex.removeKey(closed.distributionKey());
        tournamentRegisterIndex.update();
    }
    void tournamentInstanceClosed(TournamentInstanceHeader closed){
        //close enter
        this.tournamentServiceProvider.monitorInstanceOnEnd(this,closed);
    }
    void tournamentInstanceEnded(TournamentInstanceHeader ended){
        //end tournament and prize
        tournamentPlayIndex.removeKey(ended.distributionKey());
        tournamentPlayIndex.update();
        TournamentInstanceHeader _ended = _instanceIndex.remove(ended.distributionKey());
        rank(_ended);
    }
    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("tournamentId",this.distributionKey());
        jsonObject.addProperty("type",type);
        jsonObject.addProperty("name",name);
        jsonObject.addProperty("enterCost",enterCost);
        jsonObject.addProperty("scheduleId",this.index);
        return jsonObject;
    }

    @Override
    public String toString(){
        return "Tournament ["+name+"]["+distributionKey()+"]\n Start Time ["+startTime.toString()+"]\n Close Time ["+closeTime+"]\n End Time ["+endTime+"]";
    }

    public void close(){
        status = Status.CLOSED;
        this.dataStore.update(this);
    }
    public void end(){
        tournamentPlayIndex.keySet().forEach(k->{
            TournamentInstanceHeader ins  = _instanceIndex.remove(k);
            if(ins!=null) rank(ins);
        });
        status = Status.ENDED;
        this.dataStore.update(this);
        this.tournamentServiceProvider.log("Tournament ["+distributionKey()+"] ended at ["+LocalDateTime.now()+"]");
    }

    private void rank(TournamentInstanceHeader ended){
        Map<Integer,TournamentPrize> _prizes = this.tournamentServiceProvider.prize(this.index());
        int rank =1;
        for(TournamentEntry entry : ended.end()){
            entry.rank(rank);
            entry.update();
            IndexSet indexSet = new IndexSet(Tournament.HISTORY_LABEL);
            indexSet.distributionKey(entry.systemId());
            this.dataStore.createIfAbsent(indexSet,true);
            TournamentHistory history = new TournamentHistory(ended.distributionKey(),rank,entry.score(0),LocalDateTime.now());
            dataStore.create(history);
            indexSet.addKey(history.distributionKey());
            dataStore.update(indexSet);
            TournamentPrize prize = _prizes.get(rank);
            if(prize!=null) this.tournamentServiceProvider.onPrize(entry.systemId(),prize);
            rank++;
        }
    }

}
