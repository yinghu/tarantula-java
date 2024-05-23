package com.tarantula.platform.tournament;


import com.google.gson.JsonObject;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ApplicationPreSetup;

import com.icodesoftware.util.*;
import com.tarantula.game.SimpleStub;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.HashMap;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TournamentManager extends RecoverableObject implements Tournament, Portable {

    private TarantulaLogger logger = JDKLogger.getLogger(TournamentManager.class);
    private Schedule schedule;
    private String type;

    private double enterCost;
    private double credit;
    private int segmentsPerSchedule;
    private double targetScore;

    private boolean global;
    private boolean notificationOnFinish;
    private TournamentSegment[] tournamentSegments;
    private ConcurrentHashMap<Long,TournamentInstance> tournamentSegmentIndex =  new ConcurrentHashMap<>();
    private AtomicInteger segmentSlot;

    private Status status = Status.STARTING;
    private LocalDateTime startTime;
    private LocalDateTime closeTime;
    private LocalDateTime endTime;
    private int maxEntriesPerInstance;
    private int durationMinutes;

    private long startLevel;
    private long endLevel;

    private int concurrentInstanceSize;

    private TournamentRegister[] pendingInstances;
    PlatformTournamentServiceProvider tournamentServiceProvider;
    private DistributionTournamentService distributionTournamentService;
    private HashMap<Integer,TournamentPrize> prizes;

    ScheduledFuture<?> pendingSchedule;

    private ConcurrentHashMap<Long,ScheduledFuture<?>> pendingSchedules = new ConcurrentHashMap<>();


    private long scheduleId;

    private LocalDateTime nextSortingTime;

    public TournamentManager(PlatformTournamentServiceProvider platformTournamentServiceProvider){
        this.tournamentServiceProvider = platformTournamentServiceProvider;
        this.distributionTournamentService = platformTournamentServiceProvider.distributionTournamentService;
        this.dataStore = tournamentServiceProvider.dataStore;
    }
    public TournamentManager(TournamentSchedule schedule){
        this();
        this.schedule = schedule.schedule();
        this.type = schedule.type();
        this.name = schedule.name();
        this.global = schedule.global();
        this.targetScore = schedule.targetScore();
        this.segmentsPerSchedule = schedule.segmentsPerSchedule();
        if(global) {
            tournamentSegments = new TournamentSegment[this.segmentsPerSchedule];
            segmentSlot = new AtomicInteger(0);
        }
        this.notificationOnFinish = schedule.notificationOnFinish();
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
            this.startTime = schedule.startTime();
            this.endTime = schedule.endTime();
            this.closeTime = this.endTime.minusMinutes(schedule.durationMinutesPerInstance());
        }
        this.maxEntriesPerInstance = schedule.maxEntriesPerInstance();
        this.durationMinutes = schedule.durationMinutesPerInstance();
        this.enterCost = schedule.enterCost();
        this.credit = schedule.credit();
        this.startLevel = schedule.startLevel();
        this.endLevel = schedule.endLevel();
        this.scheduleId = schedule.distributionId();
    }


    public TournamentManager(){
        this.onEdge = true;
        this.label = Tournament.MANAGER_LABEL;
    }
    public Schedule schedule(){
        return this.schedule;
    }

    public boolean global(){
        return global;
    }

    public double targetScore(){
        return targetScore;
    }
    @Override
    public String type() {
        return type;
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
        buffer.writeBoolean(global);
        buffer.writeBoolean(notificationOnFinish);
        buffer.writeDouble(targetScore);
        buffer.writeLong(TimeUtil.toUTCMilliseconds(startTime));
        buffer.writeLong(TimeUtil.toUTCMilliseconds(closeTime));
        buffer.writeLong(TimeUtil.toUTCMilliseconds(endTime));
        buffer.writeInt(maxEntriesPerInstance);
        buffer.writeInt(durationMinutes);
        buffer.writeDouble(enterCost);
        buffer.writeDouble(credit);
        buffer.writeLong(scheduleId);
        buffer.writeLong(startLevel);
        buffer.writeLong(endLevel);
        buffer.writeInt(segmentsPerSchedule);
        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        schedule = Schedule.values()[buffer.readInt()];
        status = Status.values()[buffer.readInt()];
        type = buffer.readUTF8();
        name = buffer.readUTF8();
        global = buffer.readBoolean();
        notificationOnFinish = buffer.readBoolean();
        targetScore = buffer.readDouble();
        startTime = TimeUtil.fromUTCMilliseconds(buffer.readLong());
        closeTime = TimeUtil.fromUTCMilliseconds(buffer.readLong());
        endTime = TimeUtil.fromUTCMilliseconds(buffer.readLong());
        maxEntriesPerInstance = buffer.readInt();
        durationMinutes = buffer.readInt();
        enterCost = buffer.readDouble();
        credit = buffer.readDouble();
        scheduleId = buffer.readLong();
        startLevel = buffer.readLong();
        endLevel = buffer.readLong();
        segmentsPerSchedule = buffer.readInt();
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

    public long startLevel(){
        return startLevel;
    }
    public long endLevel(){
        return endLevel;
    }

    public long scheduleId(){
        return scheduleId;
    }

    private TournamentInstance lookup(long instanceId){
        TournamentInstance instance = TournamentInstance.limit(maxEntriesPerInstance,credit);
        instance.distributionId(instanceId);
        instance.label(Tournament.INSTANCE_LABEL);
        instance.ownerKey(this.key());
        LocalDateTime start = LocalDateTime.now();
        instance.started(start,start.plusMinutes(durationMinutes-tournamentServiceProvider.endBufferTimeMinutes),start.plusMinutes(durationMinutes));
        dataStore.createIfAbsent(instance,true);
        instance.dataStore(dataStore);
        instance.load();
        pendingSchedules.compute(instanceId,(k,v)->{
            if(v!=null) return v;
            return tournamentServiceProvider.schedule(new TournamentInstanceCloseMonitor(this,instance.distributionId(),instance.toClosingTime()));
        });
        return instance;
    }
    private TournamentInstance load(long instanceId){
        TournamentInstance instance = TournamentInstance.limit(maxEntriesPerInstance,credit);
        instance.distributionId(instanceId);
        if(!dataStore.load(instance)) return null;
        instance.dataStore(dataStore);
        instance.load();
        pendingSchedules.compute(instanceId,(k,v)->{
            if(v!=null) return v;
            return tournamentServiceProvider.schedule(new TournamentInstanceCloseMonitor(this,instance.distributionId(),instance.toClosingTime()));
        });
        return instance;
    }
    private TournamentInstance lookupSegmentInstance(long segmentInstanceId){
        return tournamentSegmentIndex.computeIfAbsent(segmentInstanceId,(key)->{
            TournamentInstance tournamentInstance = new TournamentInstance();
            tournamentInstance.distributionId(segmentInstanceId);
            tournamentInstance.dataStore(dataStore);
            tournamentInstance.entryDataStore = tournamentServiceProvider.tournamentEntry;
            tournamentInstance.raceBoardDataStore = tournamentServiceProvider.tournamentRaceBoard;
            if(!this.dataStore.load(tournamentInstance)) return null;
            tournamentInstance.load();
            return tournamentInstance;
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
        this.distributionId = portableReader.readLong("9");
    }

    public void setup(PlatformTournamentServiceProvider tournamentServiceProvider){
        this.tournamentServiceProvider = tournamentServiceProvider;
        this.distributionTournamentService = tournamentServiceProvider.distributionTournamentService;
        if(global){
            nextSortingTime = LocalDateTime.now().plusMinutes(tournamentServiceProvider.sortingTimerInterval.get());
            logger.warn("Next sorting time : "+nextSortingTime);
            if(status!=Status.STARTED){
                status = Status.STARTED;
                this.dataStore.update(this);
            }
            if(tournamentSegments==null){
                tournamentSegments = new TournamentSegment[segmentsPerSchedule];
                segmentSlot = new AtomicInteger(0);
            }
            TournamentInstanceQuery query = new TournamentInstanceQuery(this.distributionId,Tournament.GLOBAL_INSTANCE_LABEL);
            int[] index = {0};
            this.dataStore.list(query).forEach((ins->{
                TournamentSegment segment = new TournamentSegment();
                segment.tournamentInstance = ins;
                ins.dataStore(dataStore);
                ins.entryDataStore = tournamentServiceProvider.tournamentEntry;
                ins.raceBoardDataStore = tournamentServiceProvider.tournamentRaceBoard;
                ins.load();
                tournamentSegmentIndex.put(ins.distributionId(),ins);
                tournamentSegments[index[0]++] = segment;
            }));
            if(index[0]!=segmentsPerSchedule) throw new RuntimeException("segment not matched with ["+segmentsPerSchedule+"]");
            return;
        }
        //recovering local instances
        storeSize(this.maxEntriesPerInstance);
        this.pendingInstances = new TournamentRegister[this.concurrentInstanceSize];
        List<TournamentRegister> saved = dataStore.list(new TournamentRegisterQuery(this.key()));
        int idx = 0;
        for(TournamentRegister register : saved){
            pendingInstances[register.routingNumber()] = register;
            if(register.closed()){
                register.setup(tournamentServiceProvider,tournamentServiceProvider.nextInstanceId(),durationMinutes,maxEntriesPerInstance);
            }
            tournamentServiceProvider.logger.warn("Tournament register on slot ["+register.routingNumber()+"]");
            idx++;
        }
        for(int i=idx;i<concurrentInstanceSize;i++){
            TournamentRegister register = new TournamentRegister();
            register.ownerKey(this.key());
            register.setup(tournamentServiceProvider,tournamentServiceProvider.nextInstanceId(),durationMinutes,maxEntriesPerInstance);
            register.routingNumber(i);
            dataStore.create(register);
            pendingInstances[i]= register;
        }
        if(status==Status.STARTED) return;
        status = Status.STARTED;
        this.dataStore.update(this);
    }

    private TournamentRegisterStatus available(int slot){
        int tem = slot;
        if(tem<0 || tem> concurrentInstanceSize-1){
            tem = 0;
        }
        TournamentRegister register = pendingInstances[tem];
        if(register.available()) return new TournamentRegisterStatus(register.tournamentId(),tem);
        register.setup(tournamentServiceProvider,tournamentServiceProvider.nextInstanceId(),durationMinutes,maxEntriesPerInstance-1);//pre-cut 1
        return new TournamentRegisterStatus(register.tournamentId(),tem);
    }

    //must call from schedule threads
    void closeTournamentInstance(long closed){
        if(global) return;
        TournamentInstance instance = load(closed);
        if(instance==null){
            this.tournamentServiceProvider.logger.warn("No tournament loaded on close : "+closed);
            return;
        }
        instance.closed();
        this.dataStore.update(instance);
        pendingSchedules.remove(closed);
        pendingSchedules.putIfAbsent(closed, tournamentServiceProvider.schedule(new TournamentInstanceEndMonitor(this,instance.distributionId(),instance.toEndingTime())));
    }

    void endTournamentInstance(long ended){
        if(global) return;
        TournamentInstance instance = load(ended);
        if(instance==null){
            this.tournamentServiceProvider.logger.warn("No tournament loaded on end : "+ended);
            return;
        }
        //end tournament and prize
        rank(instance);
        instance.ended();
        this.dataStore.update(instance);
    }
    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("TournamentId",this.distributionKey());
        jsonObject.addProperty("Global",global);
        jsonObject.addProperty("TargetScore",Double.valueOf(targetScore).intValue());
        jsonObject.addProperty("NotificationOnFinish",notificationOnFinish);
        jsonObject.addProperty("Type",type);
        jsonObject.addProperty("Name",name);
        jsonObject.addProperty("StartTime",startTime.format(DateTimeFormatter.ISO_DATE_TIME));
        jsonObject.addProperty("CloseTime",closeTime.format(DateTimeFormatter.ISO_DATE_TIME));
        jsonObject.addProperty("EndTime",endTime.format(DateTimeFormatter.ISO_DATE_TIME));
        jsonObject.addProperty("NextSortingTimer",TimeUtil.durationUTCInSeconds(LocalDateTime.now(),nextSortingTime));
        jsonObject.addProperty("DurationMinutes",durationMinutes);
        jsonObject.addProperty("MaxEntries",maxEntriesPerInstance);
        jsonObject.addProperty("EnterCost",enterCost);
        jsonObject.addProperty("Credit",credit);
        jsonObject.addProperty("ScheduleId",Long.toString(this.scheduleId));
        jsonObject.addProperty("StartLevel",startLevel);
        jsonObject.addProperty("EndLevel",endLevel);
        jsonObject.addProperty("Status",status.name());
        return jsonObject;
    }

    @Override
    public String toString(){
        return "\nTournament ["+name+"] Global ["+global+"] Id ["+distributionId()+"]\n Start Time ["+startTime.toString()+"]\n Close Time ["+closeTime+"]\n End Time ["+endTime+"]\n Status ["+status+"]";
    }

    public void close(){
        status = Status.CLOSED;
        this.dataStore.update(this);
    }
    public void end(){
        if(this.global){
            for(TournamentSegment segment : tournamentSegments){
                if(!distributionTournamentService.ownership(segment.tournamentInstance.distributionId())) continue;
                rank(segment.tournamentInstance);
                segment.tournamentInstance.ended();
                this.dataStore.update(segment.tournamentInstance);
            }
        }
        status = Status.ENDED;
        this.dataStore.update(this);
    }

    private void rank(TournamentInstance ended){
        int rank =1;
        LocalDateTime endTime = LocalDateTime.now();
        for(TournamentEntry entry : ended.sorted()){
            entry.rank(rank);
            ended.entryDataStore.update(entry);
            TournamentPrize prize = prizes.get(rank);
            if(prize!=null) {
                if(notificationOnFinish){
                    this.tournamentServiceProvider.inboxServiceProvider.pendingTournamentPrize(entry.systemId(),prize);
                }
                else {
                    this.tournamentServiceProvider.inventoryServiceProvider.redeem(Long.toString(entry.systemId()), prize);
                }
            }
            TournamentHistory history = new TournamentHistory(this.distributionId,ended.distributionId(),entry.distributionId(),prize!=null? prize.distributionId() : 0,endTime);
            history.ownerKey(SnowflakeKey.from(entry.systemId()));
            this.tournamentServiceProvider.tournamentHistory.create(history);
            rank++;
        }
    }
    TournamentInstance createGlobalInstance(){
        TournamentInstance instance = TournamentInstance.global(targetScore,maxEntriesPerInstance);
        instance.label(Tournament.GLOBAL_INSTANCE_LABEL);
        instance.ownerKey(this.key());
        this.dataStore.create(instance);
        instance.entryDataStore = tournamentServiceProvider.tournamentEntry;
        instance.raceBoardDataStore = tournamentServiceProvider.tournamentRaceBoard;
        instance.load();
        return instance;
    }

    private void storeSize(int size){
        if(size<=20) {
            concurrentInstanceSize = tournamentServiceProvider.largeConcurrentInstanceSize;
            return;
        }
        if(size<=100){
            concurrentInstanceSize = tournamentServiceProvider.mediumConcurrentInstanceSize;
            return;
        }
        if(size<=1000){
            concurrentInstanceSize = tournamentServiceProvider.mediumConcurrentInstanceSize;
            return;
        }
        concurrentInstanceSize = tournamentServiceProvider.smallConcurrentInstanceSize;

    }

    void loadPrizes(ApplicationPreSetup applicationPreSetup, Descriptor application){
        this.prizes = new HashMap<>();
        TournamentSchedule schedule = new TournamentSchedule();
        schedule.distributionId(this.scheduleId);
        if(!applicationPreSetup.load(application,schedule)) return;
        schedule.setup();
        schedule.list().forEach(c-> {
            prizes.put(c.rank(),c);
        });
    }

    long toStartTime(){
        if(TimeUtil.expired(startTime)) return 1000;
        return TimeUtil.durationUTCMilliseconds(LocalDateTime.now(),startTime);
    }

    long toClosingTime(){
        if(TimeUtil.expired(closeTime)) return 1000;
        return TimeUtil.durationUTCMilliseconds(startTime,closeTime);
    }
    long toEndingTime(){
        if(TimeUtil.expired(endTime)) return 1000;
        return TimeUtil.durationUTCMilliseconds(closeTime,endTime);
    }

    private int segmentSlot(){
        return segmentSlot.getAndAccumulate(segmentsPerSchedule,(value,limit)->{
            value++;
            return value==limit? 0:value;
        });
    }

    public Instance register(Session session){
        TournamentJoin join = TournamentJoin.lookup(tournamentServiceProvider.tournamentJoin,session,scheduleId);
        if(status == Status.ENDED) return new TournamentInstanceProxy(this,join);
        if(this.global) {
            if(join.tournamentId == this.distributionId ) return new TournamentInstanceProxy(this,join);
            if(targetScore == 0) {
                TournamentInstance pending = tournamentSegments[segmentSlot()].tournamentInstance;
                long entryId = this.distributionTournamentService.onEnterGlobalTournament(tournamentServiceProvider.gameServiceName,this.distributionId,pending.distributionId(),session.distributionId());
                if(entryId==0) throw new RuntimeException("Failed to enter tournament :"+pending.distributionId());
                join.onTournament(this.distributionId,pending.distributionId(),entryId);
            }
            return new TournamentInstanceProxy(this,join);
        }
        //not used for e8 game
        if(join.tournamentId == this.distributionId) return new TournamentInstanceProxy(this,join);
        TournamentRegisterStatus pending = distributionTournamentService.onRegisterTournament(tournamentServiceProvider.gameServiceName,this.distributionId,join.slot);
        Tournament.Instance ins = this.distributionTournamentService.onEnterTournament(tournamentServiceProvider.gameServiceName,this.distributionId,pending.instanceId,session.distributionId());
        ins.distributionId(pending.instanceId);
        join.onTournament(this.distributionId,pending.slot,pending.instanceId);
        return new TournamentInstanceProxy(this,join);
    }

    public boolean enter(Session session){
        TournamentJoin join = TournamentJoin.lookup(tournamentServiceProvider.tournamentJoin,session,scheduleId);
        if(!join.closed && join.tournamentId == this.distributionId ) return true;
        TournamentInstance pending = tournamentSegments[segmentSlot()].tournamentInstance;
        long entryId = distributionTournamentService.onEnterGlobalTournament(tournamentServiceProvider.gameServiceName,this.distributionId, pending.distributionId(),session.distributionId());
        if(entryId==0) return false;
        join.onTournament(this.distributionId,pending.distributionId(),entryId);
        return true;
    }

    public double score(Session session,Entry entry){
        TournamentJoin join = TournamentJoin.lookup(tournamentServiceProvider.tournamentJoin,session,scheduleId);
        if(join.closed || join.tournamentId != this.distributionId ) return 0;
        return distributionTournamentService.onScoreGlobalTournament(tournamentServiceProvider.gameServiceName,this.distributionId,join.instanceId,join.entryId,session.distributionId(),entry.credit(),entry.score());
    }

    public double score(Session session,long instanceId,Entry entry){
        return distributionTournamentService.onScoreTournament(tournamentServiceProvider.gameServiceName,this.distributionId,instanceId,session.distributionId(),entry.credit(),entry.score());
    }
    public RaceBoard raceBoard(TournamentJoin session){
        if(session.instanceId==0) return new TournamentRaceBoard();
        byte[] payload = this.distributionTournamentService.onRaceBoard(tournamentServiceProvider.gameServiceName,distributionId,session.instanceId);
        TournamentRaceBoard raceBoard = TournamentRaceBoard.from(payload);
        raceBoard.distributionId(session.instanceId);
        return raceBoard;
    }

    public RaceBoard myRaceBoard(TournamentJoin session){
        if(session.instanceId==0) return new TournamentRaceBoard();
        byte[] payload = this.distributionTournamentService.onMyRaceBoard(tournamentServiceProvider.gameServiceName,distributionId,session.instanceId,session.entryId,session.stub());
        TournamentRaceBoard raceBoard = TournamentRaceBoard.from(payload);
        raceBoard.distributionId(session.instanceId);
        return raceBoard;
    }


    //distributed callbacks
    public long onEnterSegment(long systemId,long segmentInstanceId){
        if(!global) return 0;
        //logger.warn(distributionId+" : "+segmentInstanceId+" : "+systemId+" : joined");
        TournamentInstance segmentInstance = lookupSegmentInstance(segmentInstanceId);
        return segmentInstance.enterSegment(systemId,targetScore);
    }

    public double onScoreSegment(long systemId,long instanceId,long entryId,double credits,double score){
        if(!global) return 0;
        TournamentInstance instance = lookupSegmentInstance(instanceId);
        //logger.warn(distributionId+" : "+instanceId+" : "+systemId+" : scored");
        return instance.scoreSegment(entryId,systemId,credits,score);
    }


    public TournamentRegisterStatus onRegister(int slot){
        return available(slot);
    }

    public TournamentInstance onEnter(long systemId,long instanceId){
        if(global) return null;
        TournamentInstance instance = lookup(instanceId);
        instance.enter(systemId,0);
        return instance;
    }

    public double onScore(long systemId,long instanceId,double credits,double score){
        if(global) return 0;
        TournamentInstance instance = lookup(instanceId);
        return instance.update(new SimpleStub("",systemId), entry -> {
            entry.score(credits,score);
            return true;
        });
    }

    public RaceBoard onRaceBoard(long instanceId){
        if(global){
            TournamentInstance segment = lookupSegmentInstance(instanceId);
            if(segment==null) return new TournamentRaceBoard();
            return segment.raceBoard();
        }
        TournamentInstance instance = load(instanceId);
        if(instance==null) return new TournamentRaceBoard();
        return instance.raceBoard();
    }

    public RaceBoard onMyRaceBoard(long instanceId,long entryId,long systemId){
        if(global){
            TournamentInstance segment = lookupSegmentInstance(instanceId);
            if(segment==null) return new TournamentRaceBoard();
            return segment.myRaceBoard(entryId,systemId,tournamentServiceProvider.myRaceBoardSize);
        }
        TournamentInstance instance = load(instanceId);
        if(instance==null) return new TournamentRaceBoard();
        return instance.myRaceBoard(entryId,systemId,tournamentServiceProvider.myRaceBoardSize);
    }
}
