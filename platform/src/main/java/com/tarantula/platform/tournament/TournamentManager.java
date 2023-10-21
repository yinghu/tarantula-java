package com.tarantula.platform.tournament;


import com.google.gson.JsonObject;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Descriptor;
import com.icodesoftware.Tournament;
import com.icodesoftware.service.ApplicationPreSetup;
import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.SnowflakeKey;
import com.icodesoftware.util.TimeUtil;
import com.icodesoftware.util.ScheduleRunner;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TournamentManager extends RecoverableObject implements Tournament, Portable {

    private Schedule schedule;
    private String type;
    private String description;
    private double enterCost;
    private Status status = Status.STARTING;
    private LocalDateTime startTime;
    private LocalDateTime closeTime;
    private LocalDateTime endTime;
    private int maxEntriesPerInstance;
    private int durationMinutes;


    private ArrayBlockingQueue<TournamentInstance> pendingQueue;
    private ConcurrentHashMap<String, TournamentInstance> instanceIndex;
    private PlatformTournamentServiceProvider tournamentServiceProvider;
    private HashMap<Integer,TournamentPrize> prizes;

    ScheduledFuture<?> pendingSchedule;

    private ClusterProvider.ClusterStore tournamentStore;
    private ClusterProvider.ClusterStore[] instanceStores;
    private AtomicInteger roundRobin;

    public TournamentManager(TournamentSchedule schedule){
        this();
        this.schedule = schedule.schedule();
        this.type = schedule.type();
        this.name = schedule.name();
        this.description = schedule.description();
        if(schedule.schedule()== Schedule.DAILY_SCHEDULE){
            this.startTime = LocalDateTime.now();
            this.endTime = this.startTime.plusHours(24);
            this.closeTime = this.endTime.minusMinutes(schedule.durationMinutesPerInstance());
        }
        else if(schedule.schedule()== Schedule.WEEKLY_SCHEDULE){
            this.startTime = LocalDateTime.now();
            this.endTime = this.startTime.plusDays(7);
            this.closeTime = this.endTime.minusMinutes(schedule.durationMinutesPerInstance());
        }
        else if(schedule.schedule()== Schedule.MONTHLY_SCHEDULE){
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
        roundRobin = new AtomicInteger(0);
    }
    public Schedule schedule(){
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


    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeInt(schedule.ordinal());
        buffer.writeInt(status.ordinal());
        buffer.writeUTF8(type);
        buffer.writeUTF8(name);
        buffer.writeLong(TimeUtil.toUTCMilliseconds(startTime));
        buffer.writeLong(TimeUtil.toUTCMilliseconds(closeTime));
        buffer.writeLong(TimeUtil.toUTCMilliseconds(endTime));
        buffer.writeInt(maxEntriesPerInstance);
        buffer.writeInt(durationMinutes);
        buffer.writeDouble(enterCost);
        buffer.writeUTF8(index);
        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        schedule = Schedule.values()[buffer.readInt()];
        status = Status.values()[buffer.readInt()];
        type = buffer.readUTF8();
        name = buffer.readUTF8();
        startTime = TimeUtil.fromUTCMilliseconds(buffer.readLong());
        closeTime = TimeUtil.fromUTCMilliseconds(buffer.readLong());
        endTime = TimeUtil.fromUTCMilliseconds(buffer.readLong());
        maxEntriesPerInstance = buffer.readInt();
        enterCost = buffer.readDouble();
        index = buffer.readUTF8();
        return true;
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


    TournamentInstance lookup(String instanceId){
        return this.instanceIndex.computeIfAbsent(instanceId,(k)->{
            TournamentInstance instance = new TournamentInstance();
            instance.distributionKey(instanceId);
            if(!this.dataStore.load(instance)) return null;
            if(instance.status() == (Status.ENDED)){
                this.tournamentServiceProvider.logger.warn(instance.toString());
                return null;
            }
            instance.dataStore(dataStore);
            if(instance.status() == (Status.STARTING)){
                LocalDateTime _startTime = LocalDateTime.now();
                LocalDateTime _closeTime = _startTime.plusMinutes(durationMinutes-3);
                LocalDateTime _endTime = _startTime.plusMinutes(durationMinutes);
                instance.started(_startTime,_closeTime,_endTime,this.tournamentServiceProvider.scoreCredits);
                instance.update();
            }
            instance.load();
            instance.pendingSchedule = this.tournamentServiceProvider.serviceContext.schedule(new TournamentInstanceCloseMonitor(this,instance));
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
        portableWriter.writeLong("9",distributionId);
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
        this.distributionId = portableReader.readLong("9");
    }

    public void setup(PlatformTournamentServiceProvider tournamentServiceProvider){
        this.tournamentServiceProvider = tournamentServiceProvider;
        //recovering local instances
        instanceIndex = new ConcurrentHashMap<>();
        ArrayList<  String> synced = new ArrayList<>();
        int[] pendingPoolSize = new int[]{0};
        pendingQueue = new ArrayBlockingQueue<>(this.tournamentServiceProvider.pendingInstancePoolSizePerSchedule);
        TournamentInstanceQuery query = new TournamentInstanceQuery(this.distributionId,this.tournamentServiceProvider.serviceContext.node().nodeName());
        dataStore.list(query).forEach(instance->{
            if(instance.status() == (Status.PENDING)){
                pendingQueue.offer(instance);
                pendingPoolSize[0]++;
            }
        });
        int pendingSize = this.tournamentServiceProvider.pendingInstancePoolSizePerSchedule-pendingPoolSize[0];
        if(pendingSize>0){
            for(int i=0;i<pendingSize;i++){
                pendingQueue.offer(createInstance());
            }
        }
        this.tournamentStore = this.tournamentServiceProvider.serviceContext.clusterProvider().clusterStore(ClusterProvider.ClusterStore.SMALL,this.distributionKey(),true,false,false);
        this.instanceStores = new ClusterProvider.ClusterStore[this.tournamentServiceProvider.concurrentInstanceSize];
        String storeSize = storeSize(this.maxEntriesPerInstance);
        for(int i=0;i<this.tournamentServiceProvider.concurrentInstanceSize;i++){
            this.instanceStores[i] = tournamentServiceProvider.serviceContext.clusterProvider().clusterStore(storeSize,this.distributionKey()+"."+i,false,false,true);
            tryStartingInstance(i);
        }
        status = Status.STARTED;
        this.dataStore.update(this);
        synced.forEach(sync-> this.tournamentServiceProvider.distributionTournamentService.onSyncTournament(this.tournamentServiceProvider.gameServiceName, this.distributionKey(),sync));
        synced.clear();
    }



    public byte[] pollInstanceId(){
        if(status == Status.CLOSED || status == Status.ENDED) return null;
        int stub = roundRobin.getAndUpdate((v)->{
            v = v == this.tournamentServiceProvider.concurrentInstanceSize-1 ? 0 : (v+1);
            return v;
        });
        return instanceStores[stub].queuePoll(this.tournamentServiceProvider.instanceIdPollingTimeoutSeconds,TimeUnit.SECONDS);
    }

    void tryStartingInstance(int storeIndex){
        byte[] lockKey = this.instanceStores[storeIndex].name().getBytes();
        if(!this.tournamentStore.tryMapLock(lockKey,this.tournamentServiceProvider.clusterLockTimeoutSeconds,TimeUnit.SECONDS)) return;
        try{
            this.tournamentServiceProvider.logger.warn(instanceStores[storeIndex].name()+" : locked");
            if(!this.tournamentStore.mapExists(lockKey)){
                byte[] joinKey = startingInstance(storeIndex);
                this.tournamentStore.mapSet(lockKey,joinKey);
            }
        }finally {
            this.tournamentStore.mapUnlock(lockKey);
            this.tournamentServiceProvider.logger.warn(instanceStores[storeIndex].name()+" : unlocked");
        }
    }
    private byte[] startingInstance(int storeIndex){
        TournamentInstance instance = pendingQueue.poll();
        byte[] joinKey = instance.distributionKey().getBytes();
        for(int m=0;m<instance.maxEntries();m++){
            this.instanceStores[storeIndex].queueOffer(joinKey);
        }
        instance.starting(storeIndex);
        this.dataStore.update(instance);
        this.pendingQueue.offer(createInstance());
        return joinKey;
    }
    void closeTournamentInstanceWithFullyJoined(TournamentInstance closed){
        closed.pendingSchedule.cancel(true);
        this.tournamentServiceProvider.serviceContext.schedule(new ScheduleRunner(PlatformTournamentServiceProvider.SCHEDULE_RUNNER_DELAY,()->{
            closeTournamentInstance(closed);
        }));
    }
    //must call from schedule threads
    void closeTournamentInstance(TournamentInstance closed){
        //close enter
        byte[] lockKey = this.instanceStores[closed.routingNumber()].name().getBytes();
        this.tournamentStore.mapLock(lockKey);
        this.tournamentServiceProvider.logger.warn(instanceStores[closed.routingNumber()].name()+" : locked");
        try{
            byte[] joinKey = this.tournamentStore.mapGet(lockKey);
            if(new String(joinKey).equals(closed.distributionKey())){
                instanceStores[closed.routingNumber()].queueClear();
                joinKey = this.startingInstance(closed.routingNumber());
                this.tournamentStore.mapSet(lockKey,joinKey);
            }
        }finally {
            this.tournamentStore.mapUnlock(lockKey);
            this.tournamentServiceProvider.logger.warn(instanceStores[closed.routingNumber()].name()+" : unlocked");
        }
        closed.closed();
        this.dataStore.update(closed);
        closed.pendingSchedule = this.tournamentServiceProvider.serviceContext.schedule(new TournamentInstanceEndMonitor(this,closed));
        this.tournamentServiceProvider.logger.warn("instance closed->"+closed);
    }
    void endTournamentInstanceWithFullyFinished(TournamentInstance ended){
        ended.pendingSchedule.cancel(true);
        this.tournamentServiceProvider.serviceContext.schedule(new ScheduleRunner(PlatformTournamentServiceProvider.SCHEDULE_RUNNER_DELAY,()->{
            endTournamentInstance(ended);
        }));
    }
    void endTournamentInstance(TournamentInstance ended){
        //end tournament and prize
        TournamentInstance pendingEnded = this.instanceIndex.remove(ended.distributionKey());
        if(pendingEnded == null || pendingEnded.status() == (Status.ENDED)) return;
        if(pendingEnded.status() != (Status.CLOSED)) closeTournamentInstance(ended);
        rank(pendingEnded);
        pendingEnded.ended();
        this.dataStore.update(pendingEnded);
        this.tournamentServiceProvider.logger.warn("instance ended->"+pendingEnded);
    }
    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("TournamentId",this.distributionKey());
        jsonObject.addProperty("Type",type);
        jsonObject.addProperty("Name",name);
        jsonObject.addProperty("EnterCost",enterCost);
        jsonObject.addProperty("ScheduleId",this.index);
        return jsonObject;
    }

    @Override
    public String toString(){
        return "\nTournament ["+name+"]["+distributionKey()+"]\n Start Time ["+startTime.toString()+"]\n Close Time ["+closeTime+"]\n End Time ["+endTime+"]\n Status ["+status+"]";
    }

    public void close(){
        status = Status.CLOSED;
        this.dataStore.update(this);
    }
    public void end(){
        instanceIndex.forEach((k,ins)-> this.endTournamentInstance(ins));
        for(int i=0;i<this.tournamentServiceProvider.concurrentInstanceSize;i++){
            this.instanceStores[i].destroy();
        }
        status = Status.ENDED;
        this.dataStore.update(this);
        this.tournamentStore.destroy();
    }

    private void rank(TournamentInstance ended){
        int rank =1;
        for(TournamentEntry entry : ended.end()){
            entry.rank(rank);
            entry.update();
            TournamentHistory history = new TournamentHistory(ended.distributionKey(),rank,entry.score(),LocalDateTime.now());
            history.ownerKey(new SnowflakeKey(ended.distributionId()));
            dataStore.create(history);
            TournamentPrize prize = prizes.get(rank);
            if(prize!=null) {
                this.tournamentServiceProvider.inventoryServiceProvider.redeem(entry.systemId(),prize);
                this.tournamentServiceProvider.logger.warn(entry.systemId()+" prized");
            }
            rank++;
        }
    }
    private TournamentInstance createInstance(){
        TournamentInstance instance = new TournamentInstance(maxEntriesPerInstance);
        instance.label(this.tournamentServiceProvider.serviceContext.node().nodeName());
        instance.ownerKey(this.key());
        this.dataStore.create(instance);
        this.tournamentServiceProvider.logger.warn(instance.toString());
        return instance;
    }

    private String storeSize(int size){
        if(size<=100) return ClusterProvider.ClusterStore.SMALL;
        if(size<=1000) return ClusterProvider.ClusterStore.MEDIUM;
        return ClusterProvider.ClusterStore.LARGE;
    }

    void loadPrizes(ApplicationPreSetup applicationPreSetup, Descriptor application){
        this.prizes = new HashMap<>();
        TournamentSchedule schedule = new TournamentSchedule();
        schedule.distributionKey(this.index);
        if(!applicationPreSetup.load(application,schedule)) return;
        schedule.setup();
        schedule.list().forEach(c-> prizes.put(c.rank(),c));
    }

    long toClosingTime(){
        if(TimeUtil.expired(closeTime)) return 1000;
        return TimeUtil.durationUTCMilliseconds(startTime,closeTime);
    }
    long toEndingTime(){
        if(TimeUtil.expired(endTime)) return 1000;
        return TimeUtil.durationUTCMilliseconds(closeTime,endTime);
    }

}
