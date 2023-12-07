package com.tarantula.platform.tournament;


import com.google.gson.JsonObject;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.*;
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

public class TournamentManager extends RecoverableObject implements Tournament, Portable {

    private Schedule schedule;
    private String type;

    private double enterCost;
    private double credit;

    private double targetScore;

    private boolean global;
    private boolean notificationOnFinish;

    private Status status = Status.STARTING;
    private LocalDateTime startTime;
    private LocalDateTime closeTime;
    private LocalDateTime endTime;
    private int maxEntriesPerInstance;
    private int durationMinutes;

    private int concurrentInstanceSize;

    private TournamentRegister[] pendingInstances;
    private PlatformTournamentServiceProvider tournamentServiceProvider;
    private DistributionTournamentService distributionTournamentService;
    private HashMap<Integer,TournamentPrize> prizes;

    ScheduledFuture<?> pendingSchedule;

    private ConcurrentHashMap<Long,ScheduledFuture<?>> pendingSchedules = new ConcurrentHashMap<>();


    private long scheduleId;

    public TournamentManager(TournamentSchedule schedule){
        this();
        this.schedule = schedule.schedule();
        this.type = schedule.type();
        this.name = schedule.name();
        this.global = schedule.global();
        this.targetScore = schedule.targetScore();
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
            this.startTime = LocalDateTime.now();
            this.endTime = schedule.endTime();//this.startTime.plusHours(schedule.durationHoursPerSchedule());
            this.closeTime = this.endTime.minusMinutes(schedule.durationMinutesPerInstance());
        }
        this.maxEntriesPerInstance = schedule.maxEntriesPerInstance();
        this.durationMinutes = schedule.durationMinutesPerInstance();
        this.enterCost = schedule.enterCost();
        this.credit = schedule.credit();
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

    public long scheduleId(){
        return scheduleId;
    }

    private TournamentInstance lookup(long instanceId){
        TournamentInstance instance = new TournamentInstance(maxEntriesPerInstance,credit);
        instance.distributionId(instanceId);
        instance.label(Tournament.INSTANCE_LABEL);
        instance.ownerKey(this.key());
        LocalDateTime start = LocalDateTime.now();
        instance.started(start,start.plusMinutes(durationMinutes-1),start.plusMinutes(durationMinutes));
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
        TournamentInstance instance = new TournamentInstance(maxEntriesPerInstance,credit);
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

    private TournamentInstance globalInstance(){
        TournamentInstanceQuery query = new TournamentInstanceQuery(this.distributionId,Tournament.GLOBAL_INSTANCE_LABEL);
        TournamentInstance[] loaded = {null};
        this.dataStore.list(query,ins->{
            loaded[0] = ins;
            return false;
        });
        if(loaded[0]==null) return null;
        loaded[0].dataStore(dataStore);
        loaded[0].load();
        return loaded[0];
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
        tournamentServiceProvider.logger.warn("Tournament :"+distributionTournamentService.ownership(this.distributionId)+" : "+distributionTournamentService.partitionId(this.distributionId));
        if(global || !distributionTournamentService.ownership(this.distributionId)){
            status = Status.STARTED;
            this.dataStore.update(this);
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
                register.setup(tournamentServiceProvider.nextInstanceId(),durationMinutes,maxEntriesPerInstance);
                dataStore.update(register);
            }
            tournamentServiceProvider.logger.warn("Tournament register on slot ["+register.routingNumber()+"]");
            idx++;
        }
        for(int i=idx;i<concurrentInstanceSize;i++){
            TournamentRegister register = new TournamentRegister();
            register.ownerKey(this.key());
            register.setup(tournamentServiceProvider.nextInstanceId(),durationMinutes,maxEntriesPerInstance);
            register.routingNumber(i);
            dataStore.create(register);
            pendingInstances[i]= register;
        }
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
        register.setup(tournamentServiceProvider.nextInstanceId(),durationMinutes,maxEntriesPerInstance-1);//pre-cut 1
        dataStore.update(register);
        return new TournamentRegisterStatus(register.tournamentId(),tem);
    }



    void closeTournamentInstanceWithFullyJoined(Tournament.Instance closed){
        //closed.pendingSchedule.cancel(true);
        this.tournamentServiceProvider.serviceContext.schedule(new ScheduleRunner(PlatformTournamentServiceProvider.SCHEDULE_RUNNER_DELAY,()->{
            //closeTournamentInstance(closed);
        }));
    }
    //must call from schedule threads
    void closeTournamentInstance(long closed){
        TournamentInstance instance = load(closed);
        if(instance==null){
            this.tournamentServiceProvider.logger.warn("No tournament loaded on close : "+closed);
            return;
        }
        instance.closed();
        this.dataStore.update(instance);
        pendingSchedules.remove(closed);
        pendingSchedules.putIfAbsent(closed, tournamentServiceProvider.schedule(new TournamentInstanceEndMonitor(this,instance.distributionId(),instance.toEndingTime())));
        this.tournamentServiceProvider.logger.warn("instance closed->"+closed);
    }
    void endTournamentInstanceWithFullyFinished(Tournament.Instance ended){
        //ended.pendingSchedule.cancel(true);
        this.tournamentServiceProvider.serviceContext.schedule(new ScheduleRunner(PlatformTournamentServiceProvider.SCHEDULE_RUNNER_DELAY,()->{
            //endTournamentInstance(ended);
        }));
    }
    void endTournamentInstance(long ended){
        TournamentInstance instance = load(ended);
        if(instance==null){
            this.tournamentServiceProvider.logger.warn("No tournament loaded on end : "+ended);
            return;
        }
        //end tournament and prize
        rank(instance);
        instance.ended();
        this.dataStore.update(instance);
        this.tournamentServiceProvider.logger.warn("instance ended->"+ended);
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
        jsonObject.addProperty("DurationMinutes",durationMinutes);
        jsonObject.addProperty("MaxEntries",maxEntriesPerInstance);
        jsonObject.addProperty("EnterCost",enterCost);
        jsonObject.addProperty("Credit",credit);
        jsonObject.addProperty("ScheduleId",Long.toString(this.scheduleId));
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
        if(this.global && distributionTournamentService.ownership(this.distributionId)){
            rank(globalInstance());
        }
        status = Status.ENDED;
        this.dataStore.update(this);
    }

    private void rank(TournamentInstance ended){
        if(ended==null) return;
        int rank =1;
        for(TournamentEntry entry : ended.end()){
            entry.rank(rank);
            entry.update();
            TournamentPrize prize = prizes.get(rank);
            if(prize!=null) {
                this.tournamentServiceProvider.inventoryServiceProvider.redeem(Long.toString(entry.systemId()),prize);
            }
            rank++;
        }
    }
    private TournamentInstance createGlobalInstance(){
        TournamentInstance instance = new TournamentInstance(maxEntriesPerInstance,credit);
        instance.label(Tournament.GLOBAL_INSTANCE_LABEL);
        instance.ownerKey(this.key());
        this.dataStore.create(instance);
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

    long toClosingTime(){
        if(TimeUtil.expired(closeTime)) return 1000;
        return TimeUtil.durationUTCMilliseconds(startTime,closeTime);
    }
    long toEndingTime(){
        if(TimeUtil.expired(endTime)) return 1000;
        return TimeUtil.durationUTCMilliseconds(closeTime,endTime);
    }

    public Instance register(Session session){
        TournamentJoin join = tournamentServiceProvider.tournamentJoin(session,scheduleId);
        if(this.global) {
            if(!join.closed && join.tournamentId == this.distributionId ) return new TournamentInstanceProxy(this,join);
            join.onTournament(this.distributionId);
            if(targetScore == 0) {
                this.distributionTournamentService.onEnterTournament(tournamentServiceProvider.gameServiceName,this.distributionId,session.distributionId());
            }
            return new TournamentInstanceProxy(this,join);
        }
        if(!join.closed && join.tournamentId == this.distributionId) return new TournamentInstanceProxy(this,join);
        TournamentRegisterStatus pending = distributionTournamentService.onRegisterTournament(tournamentServiceProvider.gameServiceName,this.distributionId,join.slot);
        Tournament.Instance ins = this.distributionTournamentService.onEnterTournament(tournamentServiceProvider.gameServiceName,this.distributionId,pending.instanceId,session.distributionId());
        ins.distributionId(pending.instanceId);
        join.onTournament(this.distributionId,pending.slot,pending.instanceId);
        return new TournamentInstanceProxy(this,join);
    }

    public boolean enter(Session session){
        return distributionTournamentService.onEnterTournament(tournamentServiceProvider.gameServiceName,this.distributionId,session.distributionId());
    }
    public boolean score(Session session,Entry entry){
        return distributionTournamentService.onScoreTournament(tournamentServiceProvider.gameServiceName,this.distributionId,session.distributionId(),entry.credit(),entry.score());
    }
    public boolean score(Session session,long instanceId,Entry entry){
        return distributionTournamentService.onScoreTournament(tournamentServiceProvider.gameServiceName,this.distributionId,instanceId,session.distributionId(),entry.credit(),entry.score());
    }
    RaceBoard raceBoard(TournamentJoin session){
        if(global) {
            return distributionTournamentService.onListTournament(tournamentServiceProvider.gameServiceName, this.distributionId());
        }
        return distributionTournamentService.onListTournament(tournamentServiceProvider.gameServiceName,distributionId,session.instanceId);
    }


    //distributed callbacks
    public RaceBoard onRaceBoard(){
        TournamentInstanceQuery query = new TournamentInstanceQuery(this.distributionId,Tournament.GLOBAL_INSTANCE_LABEL);
        TournamentInstance[] loaded = {null};
        this.dataStore.list(query,ins->{
            loaded[0] = ins;
            return false;
        });
        if(loaded[0]==null) return new TournamentRaceBoard();
        loaded[0].dataStore(dataStore);
        loaded[0].load();
        return loaded[0].raceBoard();
    }
    public boolean onEnter(long systemId){
        TournamentInstanceQuery query = new TournamentInstanceQuery(this.distributionId,Tournament.GLOBAL_INSTANCE_LABEL);
        TournamentInstance[] loaded = {null};
        this.dataStore.list(query,ins->{
            loaded[0] = ins;
            return false;
        });
        if(loaded[0]==null){
            loaded[0] = createGlobalInstance();
        }
        loaded[0].dataStore(dataStore);
        loaded[0].load();
        return loaded[0].enter(systemId,targetScore);
    }

    public TournamentRegisterStatus onRegister(int slot){
        return available(slot);
    }

    public TournamentInstance onEnter(long systemId,long instanceId){
        TournamentInstance instance = lookup(instanceId);
        instance.enter(systemId);
        return instance;
    }
    public boolean onScore(long systemId,double credits,double score){
        TournamentInstanceQuery query = new TournamentInstanceQuery(this.distributionId,Tournament.GLOBAL_INSTANCE_LABEL);
        TournamentInstance[] loaded = {null};
        this.dataStore.list(query,ins->{
            loaded[0] = ins;
            return false;
        });
        if(loaded[0]==null){
            loaded[0] = createGlobalInstance();
        }
        loaded[0].dataStore(dataStore);
        loaded[0].load();
        return loaded[0].update(new SimpleStub("",systemId),entry -> {
            entry.score(credits,score);
            return entry.finished();
        });
    }
    public boolean onScore(long systemId,long instanceId,double credits,double score){
        TournamentInstance instance = lookup(instanceId);
        return instance.update(new SimpleStub("",systemId), entry -> {
            entry.score(credits,score);
            return true;
        });
    }

    public RaceBoard onRaceBoard(long instanceId){
        TournamentInstance instance = load(instanceId);
        if(instance==null) return new TournamentRaceBoard();
        return instance.raceBoard();
    }

}
