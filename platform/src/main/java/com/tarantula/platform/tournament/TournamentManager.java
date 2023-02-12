package com.tarantula.platform.tournament;


import com.google.gson.JsonObject;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Recoverable;
import com.icodesoftware.Tournament;
import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.IndexSet;
import com.tarantula.platform.event.PortableEventRegistry;
import com.tarantula.platform.util.SystemUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

public class TournamentManager extends RecoverableObject implements Tournament, Portable {


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


    public TournamentIndexSet tournamentIndexSet;

    private PlatformTournamentServiceProvider tournamentServiceProvider;

    public ScheduledFuture<?> pendingSchedule;
    private ClusterProvider.ClusterStore instanceStore;

    public TournamentManager(TournamentSchedule schedule){
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

    public TournamentManager(){

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


    public Tournament.Instance lookup(String instanceId){
        return this.tournamentServiceProvider.instanceIndex.computeIfAbsent(instanceId,(k)->{
            LocalDateTime _startTime = LocalDateTime.now();
            LocalDateTime _closeTime = _startTime.plusMinutes(durationMinutes-3);
            LocalDateTime _endTime = _startTime.plusMinutes(durationMinutes);
            TournamentInstance instance = new TournamentInstance(maxEntriesPerInstance,_startTime,_closeTime,_endTime);
            instance.distributionKey(instanceId);
            this.dataStore.createIfAbsent(instance,true);
            instance.dataStore(dataStore);
            this.tournamentServiceProvider.monitorInstanceOnClose(this,instance);
            return instance;
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

    public void setup(PlatformTournamentServiceProvider tournamentServiceProvider){
        this.tournamentServiceProvider = tournamentServiceProvider;
        this.instanceStore = tournamentServiceProvider.serviceContext.clusterProvider().clusterStore(this.distributionKey(),false,false,true);
        tournamentIndexSet = new TournamentIndexSet(this.tournamentServiceProvider.serviceContext.node().nodeName(),this.tournamentServiceProvider.maxInstancePoolSizePerZone);
        tournamentIndexSet.distributionKey(this.distributionKey());
        this.dataStore.createIfAbsent(tournamentIndexSet,true);
        tournamentIndexSet.dataStore(this.dataStore);
        this.tournamentIndexSet.keySet().forEach((k)->{
            TournamentInstance instanceHeader = new TournamentInstance();
            instanceHeader.distributionKey(k);
            if(this.dataStore.load(instanceHeader)){
                instanceHeader.dataStore(dataStore);
                instanceHeader.load();
                this.tournamentServiceProvider.instanceIndex.put(k,instanceHeader);
                if(!TimeUtil.expired(instanceHeader.closeTime())){
                    this.tournamentServiceProvider.monitorInstanceOnClose(this,instanceHeader);
                }
                else{
                    this.tournamentServiceProvider.log("Expired tournament instance scheduled to end->"+instanceHeader.distributionKey());
                    this.tournamentServiceProvider.monitorInstanceOnEnd(this,instanceHeader);
                }
            }
        });
        int created = this.tournamentServiceProvider.maxInstancePoolSizePerZone - this.tournamentIndexSet.remaining();
        if( created < this.tournamentServiceProvider.minInstancePoolSizePerZone){
            int initPool = this.tournamentServiceProvider.minInstancePoolSizePerZone-created;
            this.tournamentServiceProvider.logger.warn("Creating instance count ->"+initPool);
            for(int i=0;i<initPool;i++){
                String pendingId = this.pendingInstanceId();
                if(tournamentIndexSet.addKey(pendingId)){
                    byte[] cid = pendingId.getBytes();
                    for(int p=0;p<this.maxEntriesPerInstance;p++){
                        this.instanceStore.queueOffer(cid);
                    }
                }
            }
            this.dataStore.update(tournamentIndexSet);
        }
        status = Status.STARTED;
        this.dataStore.update(this);
    }

    public String pollInstanceId(){
        byte[] pendingId = instanceStore.queuePoll();
        if(pendingId==null){
            String newId = pendingInstanceId();
            if(tournamentIndexSet.remaining() >0 && tournamentIndexSet.addKey(newId)){
                byte[] cid = newId.getBytes();
                for(int i =1; i<maxEntriesPerInstance;i++){
                    this.instanceStore.queueOffer(cid);
                }
                this.dataStore.update(tournamentIndexSet);
                pendingId = cid;
            }
        }
        if(pendingId==null) return null;
        return new String(pendingId);
    }

    void tournamentInstanceClosed(TournamentInstance closed){
        //close enter
        this.tournamentServiceProvider.monitorInstanceOnEnd(this,closed);
    }
    void tournamentInstanceEnded(TournamentInstance ended){
        //end tournament and prize
        tournamentIndexSet.removeKey(ended.distributionKey());
        tournamentIndexSet.update();
        TournamentInstance _ended = this.tournamentServiceProvider.instanceIndex.remove(ended.distributionKey());
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
        tournamentIndexSet.keySet().forEach(k->{
            TournamentInstance ins  = this.tournamentServiceProvider.instanceIndex.remove(k);
            if(ins!=null) rank(ins);
        });
        status = Status.ENDED;
        this.dataStore.update(this);
        this.instanceStore.clear();
        this.tournamentServiceProvider.log("Tournament ["+distributionKey()+"] ended at ["+LocalDateTime.now()+"]");
    }

    private void rank(TournamentInstance ended){
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

    private String pendingInstanceId(){
        return this.tournamentServiceProvider.serviceContext.node().bucketName()+ Recoverable.PATH_SEPARATOR+ SystemUtil.oid();
    }

}
