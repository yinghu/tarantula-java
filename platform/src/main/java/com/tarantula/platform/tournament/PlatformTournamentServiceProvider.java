package com.tarantula.platform.tournament;

import com.icodesoftware.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.*;
import com.icodesoftware.util.BufferUtil;
import com.icodesoftware.util.SnowflakeKey;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.game.SimpleStub;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.GameCluster;
import com.icodesoftware.util.ScheduleRunner;
import com.tarantula.platform.inventory.PlatformInventoryServiceProvider;
import com.tarantula.platform.item.ConfigurableObject;
import com.tarantula.platform.item.DistributionItemService;
import com.tarantula.platform.item.ItemDistributionCallback;
import com.tarantula.platform.service.SystemValidatorProvider;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class PlatformTournamentServiceProvider implements TournamentServiceProvider, ReloadListener, ConfigurationServiceProvider, ItemDistributionCallback {

    private static final String CONFIG = "game-tournament-settings";

    public static final String NAME = "tournament";

    //private final static String TOURNAMENT_LOOKUP_INDEX = "tournament";

    final static long SCHEDULE_RUNNER_DELAY = 500;

    TarantulaLogger logger;

    ServiceContext serviceContext;

    DistributionTournamentService distributionTournamentService;

    private DistributionItemService distributionItemService;
    final String gameServiceName;
    private DataStore dataStore;
    private CopyOnWriteArrayList<Tournament.Listener> listeners = new CopyOnWriteArrayList<>();

    private ConcurrentHashMap<Long,TournamentManager> tournamentIndex = new ConcurrentHashMap<>();


    int smallConcurrentInstanceSize = 3;
    int mediumConcurrentInstanceSize = 20;

    int largeConcurrentInstanceSize = 100;

    int minDurationHoursPerSchedule = 1;
    int minDurationMinutesPerInstance =  5;
    int endBufferTimeMinutes = 3;
    double scoreCredits;
    int clusterLockTimeoutSeconds = 5;
    int instanceIdPollingTimeoutSeconds = 3;
    int instanceIdPollingRetries =3;
    int maxPlayerHistoryRecords = 10;

    private String reloadKey;
    private final GameCluster gameCluster;
    private ApplicationPreSetup applicationPreSetup;
    private Descriptor application;
    PlatformInventoryServiceProvider inventoryServiceProvider;
    TokenValidatorProvider systemValidatorProvider;
    private ClusterProvider.ClusterStore scheduleStore;

    public PlatformTournamentServiceProvider(PlatformGameServiceProvider gameServiceProvider){
        this.gameCluster = gameServiceProvider.gameCluster();
        this.gameServiceName = this.gameCluster.serviceType();
        this.inventoryServiceProvider = gameServiceProvider.inventoryServiceProvider();
    }

    @Override
    public void registerTournamentListener(Tournament.Listener listener) {
        listeners.add(listener);
        tournamentIndex.forEach(((k,tournament)->{
            listener.tournamentStarted(tournament);
        }));
    }
    @Override
    public String registerConfigurableListener(Descriptor descriptor, Configurable.Listener listener) {
        this.application = descriptor;
        this.tournamentIndex.forEach((k,t)->t.loadPrizes(applicationPreSetup,application));
        scheduleTournament();
        return null;
    }

    public Tournament tournament(long tournamentId){
        return tournamentIndex.get(tournamentId);
    }
    @Override
    public boolean available(long tournamentId) {
        return tournamentIndex.get(tournamentId) != null;
    }

    @Override
    public Tournament.Instance enter(String tournamentId, String systemId) {
        TournamentManager index = tournamentIndex.get(tournamentId);
        byte[] pendingId = null;
        for(int retry = 0;retry < this.instanceIdPollingRetries;retry++){
            //pendingId = index.pollInstanceId();
            if(pendingId != null) break;
        }
        if(pendingId == null) return null;
        String instanceId = new String(pendingId);
        //Tournament.Instance instance = this.distributionTournamentService.onEnterTournament(gameServiceName,tournamentId,instanceId,systemId);
        //instance.distributionKey(instanceId);
        return null;//instance;
    }

    @Override
    public Tournament.Entry score(String tournamentId,String instanceId, String systemId, double credit,double delta) {
        //Tournament.Entry _e = this.distributionTournamentService.onScoreTournament(gameServiceName,tournamentId,instanceId,systemId,credit,delta);
        //logger.warn(_e.toJson().toString());
        return null;//_e;
    }

    public void finish(String tournamentId,String instanceId, String systemId){
        this.distributionTournamentService.onFinishTournament(gameServiceName,tournamentId,instanceId,systemId);
        //Tournament.RaceBoard board = this.distributionTournamentService.onListTournament(gameServiceName,tournamentId,instanceId);
        //logger.warn(board.toJson().toString());
    }
    @Override
    public Tournament.RaceBoard list(String tournamentId,String instanceId) {
       // Tournament.RaceBoard ins = this.distributionTournamentService.onListTournament(gameServiceName,tournamentId,instanceId);
        //Collections.sort(ins.list(),new TournamentEntryComparator());
        //return ins;
        return null;
    }

    @Override
    public Tournament.RaceBoard list(String tournamentId) {
        return this.distributionTournamentService.onListTournament(gameServiceName,Long.parseLong(tournamentId));
    }
    public List<Tournament> list(){
        ArrayList<Tournament> _tms = new ArrayList<>();
        tournamentIndex.forEach((k,v)->
        {
            if(v.status() == Tournament.Status.STARTED) _tms.add(v);
        });
        return _tms;
    }
    public String name(){
        return NAME;
    }
    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        this.applicationPreSetup = gameCluster.applicationPreSetup();
        Configuration configuration = serviceContext.configuration(CONFIG);
        this.smallConcurrentInstanceSize = ((Number)configuration.property("smallConcurrentInstanceSize")).intValue();
        this.mediumConcurrentInstanceSize = ((Number)configuration.property("mediumConcurrentInstanceSize")).intValue();
        this.largeConcurrentInstanceSize = ((Number)configuration.property("largeConcurrentInstanceSize")).intValue();
        this.minDurationHoursPerSchedule = ((Number)configuration.property("minDurationHoursPerSchedule")).intValue();
        this.minDurationMinutesPerInstance = ((Number)configuration.property("minDurationMinutesPerInstance")).intValue();
        this.endBufferTimeMinutes = ((Number)configuration.property("endBufferTimeMinutes")).intValue();
        this.scoreCredits = ((Number)configuration.property("scoreCredits")).doubleValue();
        this.maxPlayerHistoryRecords = ((Number)configuration.property("maxPlayerHistoryRecords")).intValue();
        this.clusterLockTimeoutSeconds = ((Number)configuration.property("clusterLockTimeoutSeconds")).intValue();
        this.instanceIdPollingTimeoutSeconds = ((Number)configuration.property("instanceIdPollingTimeoutSeconds")).intValue();
        this.instanceIdPollingRetries = ((Number)configuration.property("instanceIdPollingRetries")).intValue();
        this.dataStore = applicationPreSetup.dataStore(gameCluster,name());
        this.logger = JDKLogger.getLogger(PlatformTournamentServiceProvider.class);
        this.reloadKey = this.serviceContext.clusterProvider().registerReloadListener(this);
        this.distributionTournamentService = this.serviceContext.clusterProvider().serviceProvider(DistributionTournamentService.NAME);
        this.distributionItemService = this.serviceContext.clusterProvider().serviceProvider(DistributionItemService.NAME);
        this.scheduleStore = this.serviceContext.clusterProvider().clusterStore(ClusterProvider.ClusterStore.SMALL,gameCluster.typeId()+"."+NAME);
        this.systemValidatorProvider = (TokenValidatorProvider)serviceContext.serviceProvider(TokenValidatorProvider.NAME);
    }

    @Override
    public void start() throws Exception {
        TournamentManagerQuery query = new TournamentManagerQuery(this.serviceContext.node().nodeId(),this.serviceContext.node().nodeName());
        dataStore.list(query).forEach((tournament)->{
            if(tournament.status() != Tournament.Status.ENDED && tournament.status() != Tournament.Status.CLOSED ){
                TournamentScheduleStatus status = new TournamentScheduleStatus();
                status.distributionId(tournament.scheduleId());
                this.dataStore.load(status);
                byte[] lockKey = status.key().asBinary();
                try{
                    scheduleStore.mapLock(lockKey);
                    if(!this.scheduleStore.mapExists(lockKey)){
                        this.scheduleStore.mapSet(lockKey,status.toBinary());
                        this.distributionItemService.onRegisterItem(gameServiceName,name(),"TournamentSchedule",tournament.distributionKey());
                    }
                    else{
                        tournament.dataStore(this.dataStore);
                        launch(tournament);
                    }
                }finally {
                    scheduleStore.mapUnlock(lockKey);
                }
            }
        });
        this.logger.warn("Tournament service provider started with concurrent tournament pool size->[ on game service ["+gameServiceName+"]["+gameCluster.name()+"]");
    }

    @Override
    public void shutdown() throws Exception {
        this.logger.warn("distributed tournament shutdown");
        this.serviceContext.clusterProvider().unregisterReloadListener(reloadKey);
    }

    public List<Tournament.History> playerHistory(String systemId){
        ArrayList<Tournament.History> list = new ArrayList<>();
        return list;
    }
    public Tournament.Instance tournamentHistory(String tournamentId){//schedule node
        TournamentHistoryRecord tournament = new TournamentHistoryRecord();
        tournament.distributionKey(tournamentId);
        tournament.dataStore(dataStore);
        if(!this.dataStore.load(tournament)){
            return null;
        }
        tournament.load();
        return tournament;
    }

    @Override
    public void reload(int partition,boolean localMember) {
        tournamentIndex.clear();
    }
    public void atMidnight(){
        serviceContext.schedule(new ScheduleRunner(SCHEDULE_RUNNER_DELAY,()->{
            logger.warn("Running midnight check tasks ->"+gameServiceName);
            byte[] pendingSchedule;
            do{
                //midnight scheduling
                pendingSchedule = this.scheduleStore.queuePoll();
                if(pendingSchedule!=null){
                    TournamentSchedule schedule = this.tournamentSchedule(BufferUtil.toLong(pendingSchedule));
                    if(schedule!=null && !schedule.configurableObject.disabled()){
                        registerTournament(schedule);
                    }
                }
            }while(pendingSchedule!=null);
            scheduleTournament();
        }));
    }
    private void scheduleTournament(){
        LocalDateTime _current = LocalDateTime.now();
        TournamentScheduleStatusQuery query = new TournamentScheduleStatusQuery(this.serviceContext.node().nodeId(),TournamentScheduleStatus.TOURNAMENT_SCHEDULE_LOOKUP_INDEX);
        dataStore.list(query).forEach(ts->{
            TournamentSchedule schedule = this.tournamentSchedule(ts.distributionId());
            if(schedule!=null){
                if(schedule.startTime().getDayOfYear() == _current.plusDays(1).getDayOfYear()){
                    scheduleStore.queueOffer(BufferUtil.fromLong(ts.distributionId()));
                }
            }
        });
    }


    void closeTournament(TournamentManager tournament){
        this.listeners.forEach(l->l.tournamentClosed(tournament));
        byte[] lockKey = tournament.key().asBinary();
        try {
            scheduleStore.mapLock(lockKey);
            tournament.close();
            this.serviceContext.schedule(new TournamentEndMonitor(tournament, this));
        }finally {
            scheduleStore.mapUnlock(lockKey);
        }
    }
    void endTournament(TournamentManager tournament){
        byte[] lockKey = tournament.key().asBinary();
        try {
            scheduleStore.mapLock(lockKey);
            tournament.end();
            if(scheduleStore.mapExists(lockKey)) {
                scheduleStore.mapRemove(lockKey);
                clearTournament(tournament);
                this.distributionItemService.onReleaseItem(gameServiceName, name(), "TournamentSchedule", tournament.distributionKey());
            }
        }
        finally {
            scheduleStore.mapUnlock(lockKey);
        }

    }

    ///schedule and launch
    @Override
    public <T extends Configurable> void register(T t) {
        if(!t.configurationCategory().equals("TournamentSchedule")) throw new RuntimeException(t.configurationCategory()+" cannot be registered");
        byte[] lockKey = t.key().asBinary();
        try{
            scheduleStore.mapLock(lockKey);
            TournamentSchedule schedule = new TournamentSchedule((ConfigurableObject) t);
            TournamentScheduleStatus status = schedule.status();
            dataStore.createIfAbsent(status,true);
            if(status.status != Tournament.Status.PENDING ) throw new RuntimeException("schedule is running on tournament ["+status.tournamentId+"]");
            if(TimeUtil.durationUTCInHours(schedule.startTime(),schedule.endTime()) < minDurationHoursPerSchedule) throw new RuntimeException("min hours per schedule less than ["+minDurationHoursPerSchedule+"]");
            if(schedule.durationMinutesPerInstance() < minDurationMinutesPerInstance) throw new RuntimeException("min minutes per instance less than ["+minDurationMinutesPerInstance+"]");
            switch (schedule.schedule()){
                case DAILY_SCHEDULE:
                case WEEKLY_SCHEDULE:
                case MONTHLY_SCHEDULE:
                    registerSchedule(schedule);
                    break;
                case ON_DEMAND_SCHEDULE:
                    registerTournament(schedule);
                    break;
                default:
                    throw new RuntimeException("schedule plan not supported ["+schedule.schedule()+"]");
            }
            t.registered();
        }finally {
            scheduleStore.mapUnlock(lockKey);
        }
    }
    @Override
    public <T extends Configurable> void release(T t) {
        byte[] lockKey = t.distributionKey().getBytes();
        try {
            scheduleStore.mapLock(lockKey);
            TournamentScheduleStatus status = new TournamentScheduleStatus();
            status.distributionId(t.distributionId());
            if(!dataStore.load(status)) throw new RuntimeException("Tournament schedule not installed");
            if (status.status == Tournament.Status.PENDING) { //cancel schedule
                dataStore.delete(status);
                t.released();
                return;
            }
            if(status.status == Tournament.Status.STARTING) throw new RuntimeException("Tournament cannot be canceled during starting.");
            //forcefully end tournament
            distributionTournamentService.onEndTournament(gameServiceName, Long.toString(status.tournamentId));
            t.released();
        }finally {
            scheduleStore.mapUnlock(lockKey);
        }
    }

    @Override
    public boolean onItemRegistered(String category, String itemId) {
        TournamentManager tournament = new TournamentManager();
        tournament.distributionKey(itemId);
        if (!this.dataStore.load(tournament)) {
            return false;
        }
        tournament.dataStore(dataStore);
        launch(tournament);
        listeners.forEach(l->l.tournamentStarted(tournament));
        return true;
    }

    @Override
    public boolean onItemReleased(String category, String itemId) {
        TournamentManager index = tournamentIndex.remove(itemId);
        if(index == null) return false;
        listeners.forEach(l->l.tournamentEnded(index));
        return true;
    }

    private void registerSchedule(TournamentSchedule schedule){
        LocalDateTime _current = LocalDateTime.now();
        if(TimeUtil.expired(schedule.startTime())
                || (schedule.startTime().getYear() ==_current.getYear() && schedule.startTime().getDayOfYear() ==_current.getDayOfYear())) throw new RuntimeException("start time already expired on daily midnight launch");
        if(schedule.startTime().getDayOfYear() == _current.plusDays(1).getDayOfYear()){
            this.scheduleStore.queueOffer(BufferUtil.fromLong(schedule.distributionId()));
        }
    }
    private void registerTournament(TournamentSchedule schedule){
        TournamentScheduleStatus status = schedule.status();
        if(!this.dataStore.load(status)) throw new RuntimeException("no schedule installed");
        TournamentManager tournament = new TournamentManager(schedule);
        tournament.dataStore(dataStore);
        tournament.label(this.serviceContext.node().nodeName());
        tournament.ownerKey(new SnowflakeKey(this.serviceContext.node().nodeId()));
        tournament.onEdge(true);
        if(!dataStore.create(tournament)) throw new RuntimeException("Failed to create tournament instance");
        status.status = Tournament.Status.STARTING;
        status.tournamentId = tournament.distributionId();
        dataStore.update(status);
        this.serviceContext.schedule(new ScheduleRunner(SCHEDULE_RUNNER_DELAY,()->{
            byte[] lockKey = tournament.key().asBinary();
            try{
                this.scheduleStore.mapLock(lockKey);
                status.status = Tournament.Status.STARTED;
                this.scheduleStore.mapSet(lockKey,status.toBinary());//
                this.distributionItemService.onRegisterItem(gameServiceName,name(),"TournamentSchedule",tournament.distributionKey());
                dataStore.update(status);
            }finally {
                this.scheduleStore.mapUnlock(lockKey);
            }
        }));
    }
    private void launch(TournamentManager tournament){
        this.tournamentIndex.put(tournament.distributionId(),tournament);
        tournament.setup(this);
        tournament.pendingSchedule = this.serviceContext.schedule(new TournamentCloseMonitor(tournament,this));
        logger.warn(tournament.toString());
        if(this.application==null) return;
        tournament.loadPrizes(this.applicationPreSetup,this.application);
    }

    private void clearTournament(TournamentManager tournament){
        TournamentScheduleStatus status = new TournamentScheduleStatus();
        status.distributionKey(tournament.index());
        this.dataStore.delete(status);
        ConfigurableObject schedule = this.tournamentSchedule(Long.parseLong(tournament.index())).configurableObject;
        schedule.released();
    }
    private TournamentSchedule tournamentSchedule(long scheduleId){
        ConfigurableObject schedule = new ConfigurableObject();
        schedule.distributionId(scheduleId);
        return applicationPreSetup.load(application,schedule) ? new TournamentSchedule(schedule) : null;
    }

    OnSession onSession(Session session){
        return systemValidatorProvider.onSession(session);
    }
    long nextInstanceId(){
        return serviceContext.distributionId();
    }

    //distributed operation callbacks
    public TournamentRegisterStatus onTournamentRegistered(long tournamentId,int slot){
        TournamentManager tournamentManager = this.tournamentIndex.get(tournamentId);
        return tournamentManager.onRegister(slot);
    }
    public boolean onTournamentEntered(long tournamentId,long systemId){
        TournamentManager tournamentManager = this.tournamentIndex.get(tournamentId);
        boolean joined = tournamentManager.onEnter(systemId);
        return joined;
    }
    public Tournament.Instance onTournamentEntered(long tournamentId,long instanceId,long systemId){
        TournamentManager tournamentManager = this.tournamentIndex.get(tournamentId);
        return tournamentManager.onEnter(systemId,instanceId);
    }
    public boolean onTournamentScored(long tournamentId,long instanceId, long systemId, double credit,double delta){
        TournamentManager tournamentManager = this.tournamentIndex.get(tournamentId);
        return tournamentManager.onScore(systemId,instanceId,credit,delta);
        //Tournament.Instance _ins = tournamentManager.lookup(instanceId);
        //if(_ins==null) return new TournamentEntry();
        //Tournament.Entry[] score={null};
        //if(_ins.update(new SimpleStub(systemId,0),(e)->{
            //e.score(credit,delta);
            //score[0]=e;
            //return e.finished();
        //})) tournamentManager.endTournamentInstanceWithFullyFinished(_ins);
        //return score[0];
    }
    public Tournament.RaceBoard onTournamentListed(long tournamentId,long instanceId){
        TournamentManager tournamentManager = this.tournamentIndex.get(tournamentId);
        return tournamentManager.onRaceBoard(instanceId);
    }

    public Tournament.RaceBoard onTournamentListed(long tournamentId){
        TournamentManager tournamentManager = this.tournamentIndex.get(tournamentId);
        if(tournamentManager == null) return new TournamentRaceBoard();
        return tournamentManager.onRaceBoard();
    }
    public void onTournamentFinished(String tournamentId,String instanceId,String systemId){
        TournamentManager tournamentManager = this.tournamentIndex.get(tournamentId);
        //Tournament.Instance _ins = tournamentManager.lookup(instanceId);
        //if(_ins==null) return;
        //if(_ins.update(new SimpleStub(systemId,0),e->{
            //e.finish();
            //return e.finished();
        //})) tournamentManager.endTournamentInstanceWithFullyFinished(_ins);
    }

    public void onTournamentSynced(String tournamentId,String instanceId){
        TournamentManager tournamentManager = this.tournamentIndex.get(tournamentId);
        //Tournament.Instance synced = tournamentManager.lookup(instanceId);
        //logger.warn("SYNC Instance : "+synced);
    }
    public void onTournamentClosed(String tournamentId){
        TournamentManager index = tournamentIndex.get(tournamentId);
        if(index==null) return;
        index.close();
    }
    public void onTournamentEnded(String tournamentId){
        logger.warn("Tournament forcefully end ->"+tournamentId);
        TournamentManager tournament = tournamentIndex.get(tournamentId);
        if(tournament == null ) return;
        tournament.pendingSchedule.cancel(true);
        this.serviceContext.schedule(new ScheduleRunner(SCHEDULE_RUNNER_DELAY,()->{
            endTournament(tournament);
        }));
    }


}
