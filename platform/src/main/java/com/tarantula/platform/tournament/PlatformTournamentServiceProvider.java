package com.tarantula.platform.tournament;

import com.icodesoftware.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.*;
import com.icodesoftware.util.BufferUtil;
import com.icodesoftware.util.SnowflakeKey;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.GameCluster;
import com.icodesoftware.util.ScheduleRunner;
import com.tarantula.platform.inbox.PlatformInboxServiceProvider;
import com.tarantula.platform.inventory.PlatformInventoryServiceProvider;
import com.tarantula.platform.item.ConfigurableObject;
import com.tarantula.platform.item.DistributionItemService;
import com.tarantula.platform.item.ItemDistributionCallback;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class PlatformTournamentServiceProvider implements TournamentServiceProvider, ReloadListener, ConfigurationServiceProvider, ItemDistributionCallback {

    private static final String CONFIG = "game-tournament-settings";

    static final String TOURNAMENT_DATA_STORE = "tournament";
    static final String RECENTLY_TOURNAMENT_INDEX_DATA_STORE = "tournament_recently_index";
    static final String TOURNAMENT_JOIN_DATA_STORE = "tournament_join";
    static final String TOURNAMENT_ENTRY_DATA_STORE = "tournament_entry";
    static final String TOURNAMENT_RACE_BOARD_DATA_STORE = "tournament_race_board";
    static final String TOURNAMENT_HISTORY_DATA_STORE = "tournament_history";


    public static final String NAME = "tournament";

    final static long SCHEDULE_RUNNER_DELAY = 500;

    TarantulaLogger logger;

    ServiceContext serviceContext;

    DistributionTournamentService distributionTournamentService;

    private DistributionItemService distributionItemService;
    final String gameServiceName;

    DataStore dataStore;

    DataStore recentlyTournamentIndex;

    DataStore tournamentJoin;

    DataStore tournamentEntry;

    DataStore tournamentRaceBoard;

    DataStore tournamentHistory;

    private CopyOnWriteArrayList<Tournament.Listener> listeners = new CopyOnWriteArrayList<>();

    private ConcurrentHashMap<Long,TournamentManager> tournamentIndex = new ConcurrentHashMap<>();

    //private ConcurrentHashMap<String,RecentlyTournamentList> typedTournamentIndex = new ConcurrentHashMap<>();



    int smallConcurrentInstanceSize = 3;
    int mediumConcurrentInstanceSize = 20;

    int largeConcurrentInstanceSize = 100;

    int minDurationHoursPerSchedule = 1;
    int minDurationMinutesPerInstance =  5;
    int endBufferTimeMinutes = 3;

    int clusterLockTimeoutSeconds = 5;

    int maxPlayerHistoryRecords = 10;
    int recentlyTournamentListSize;
    int topRaceBoardSize;
    int myRaceBoardAheadNumber;
    int myRaceBoardBehindNumber;
    boolean localOperationEnabled;
    AtomicInteger snapshotTimerInterval = new AtomicInteger(0);
    private String reloadKey;
    final GameCluster gameCluster;
    private ApplicationPreSetup applicationPreSetup;
    private Descriptor application;
    PlatformInventoryServiceProvider inventoryServiceProvider;
    PlatformInboxServiceProvider inboxServiceProvider;

    TokenValidatorProvider systemValidatorProvider;
    private ClusterProvider.ClusterStore scheduleStore;

    public PlatformTournamentServiceProvider(PlatformGameServiceProvider gameServiceProvider){
        this.gameCluster = gameServiceProvider.gameCluster();
        this.gameServiceName = this.gameCluster.serviceType();
        this.inventoryServiceProvider = gameServiceProvider.inventoryServiceProvider();
        this.inboxServiceProvider = gameServiceProvider.inboxServiceProvider();
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

    void loadPrizes(TournamentManager tournamentManager){
        tournamentManager.loadPrizes(applicationPreSetup,application);
    }

    public Tournament tournament(long tournamentId){
        TournamentManager indexed = tournamentIndex.get(tournamentId);
        if(indexed!=null) return indexed;
        indexed = new TournamentManager(this);
        indexed.distributionId(tournamentId);
        if(!dataStore.load(indexed)) throw new RuntimeException("Tournament ["+tournamentId+"] not existed");
        tournamentIndex.putIfAbsent(tournamentId,indexed);
        return indexed;
    }
    @Override
    public boolean available(long tournamentId) {
        return tournamentIndex.get(tournamentId) != null;
    }

    public List<Tournament> list(){
        ArrayList<Tournament> _tms = new ArrayList<>();
        tournamentIndex.forEach((k,v)->
        {
            if(v.status() == Tournament.Status.STARTED) _tms.add(v);
        });
        return _tms;
    }

    public List<Tournament> list(String type){
        if(type==null || type.trim().length() < 4) throw new RuntimeException("query type cannot be null or less than 4 chars");
        ArrayList<Tournament> _tms = new ArrayList<>();
        RecentlyTournamentList list = RecentlyTournamentList.lookup(recentlyTournamentIndex,type,recentlyTournamentListSize);
        Long[] tournaments = list.pop();
        for(Long id : tournaments){
            if(id==null || id==0) continue;
            TournamentManager tournamentManager = tournamentIndex.get(id);
            if(tournamentManager!=null) {
                _tms.add(tournamentManager);
                continue;
            }
            tournamentManager = new TournamentManager();
            tournamentManager.distributionId(id);
            if(!dataStore.load(tournamentManager)) continue;
            tournamentManager.tournamentServiceProvider = this;
            _tms.add(tournamentManager);
        }
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
        this.maxPlayerHistoryRecords = ((Number)configuration.property("maxPlayerHistoryRecords")).intValue();
        this.clusterLockTimeoutSeconds = ((Number)configuration.property("clusterLockTimeoutSeconds")).intValue();
        this.recentlyTournamentListSize = ((Number)configuration.property("recentlyTournamentListSize")).intValue();
        this.topRaceBoardSize = ((Number)configuration.property("topRaceBoardSize")).intValue();
        this.myRaceBoardAheadNumber = ((Number)configuration.property("myRaceBoardAheadNumber")).intValue();
        this.myRaceBoardBehindNumber = ((Number)configuration.property("myRaceBoardBehindNumber")).intValue();
        this.snapshotTimerInterval.set(((Number)configuration.property("snapshotIntervalMinutes")).intValue());
        this.localOperationEnabled = (boolean)configuration.property("localOperationEnabled");
        this.dataStore = applicationPreSetup.dataStore(gameCluster,TOURNAMENT_DATA_STORE);
        //String localIndexDataStoreName = gameCluster.serviceType().replaceAll("-","_")+"_"+NAME+"_local_index";
        //this.recentlyTournamentIndex = serviceContext.dataStore(Distributable.LOCAL_SCOPE,localIndexDataStoreName);
        this.recentlyTournamentIndex = applicationPreSetup.dataStore(gameCluster,RECENTLY_TOURNAMENT_INDEX_DATA_STORE);
        this.tournamentJoin = applicationPreSetup.dataStore(gameCluster,TOURNAMENT_JOIN_DATA_STORE);
        this.tournamentEntry = applicationPreSetup.dataStore(gameCluster,TOURNAMENT_ENTRY_DATA_STORE);
        this.tournamentRaceBoard = applicationPreSetup.dataStore(gameCluster,TOURNAMENT_RACE_BOARD_DATA_STORE);
        this.tournamentHistory = applicationPreSetup.dataStore(gameCluster,TOURNAMENT_HISTORY_DATA_STORE);
        this.logger = JDKLogger.getLogger(PlatformTournamentServiceProvider.class);
        this.reloadKey = this.serviceContext.clusterProvider().registerReloadListener(this);
        this.distributionTournamentService = this.serviceContext.clusterProvider().serviceProvider(DistributionTournamentService.NAME);
        this.distributionItemService = this.serviceContext.clusterProvider().serviceProvider(DistributionItemService.NAME);
        this.scheduleStore = this.serviceContext.clusterProvider().clusterStore(ClusterProvider.ClusterStore.SMALL,gameCluster.typeId()+"."+NAME);
        this.systemValidatorProvider = (TokenValidatorProvider)serviceContext.serviceProvider(TokenValidatorProvider.NAME);
    }

    @Override
    public void start() throws Exception {
        //recentlyTournamentIndex.list(new RecentlyTournamentListQuery(gameCluster.distributionId()),list->{
            //if(list.name()!=null) {
                //list.dataStore(recentlyTournamentIndex);
                //typedTournamentIndex.put(list.name(),list);
            //}
            //return true;
        //});
        dataStore.list(new TournamentScheduleStatusQuery(this.gameCluster.distributionId())).forEach(status->{
            logger.warn("Tournament Status : "+status.tournamentId+" : "+status.status);
            byte[] lockKey = status.key().asBinary();
            try{
                scheduleStore.mapLock(lockKey);
                if(status.status != Tournament.Status.PENDING){
                    TournamentManager tournament = new TournamentManager();
                    tournament.distributionId(status.tournamentId);
                    tournament.dataStore(dataStore);
                    if(dataStore.load(tournament)){
                        launch(tournament);
                    }
                    else{
                        logger.warn("Tournament cannot be loaded ["+status.tournamentId+"]");
                    }
                }
            }
            finally {
                scheduleStore.mapUnlock(lockKey);
            }
        });
        this.logger.warn("Tournament service provider started on ["+gameServiceName+"]["+gameCluster.name()+"] with local operation enabled ["+localOperationEnabled+"]");
    }

    @Override
    public void shutdown() throws Exception {
        this.logger.warn("distributed tournament shutdown");
        this.serviceContext.clusterProvider().unregisterReloadListener(reloadKey);
    }

    @Override
    public void reload(int partition,boolean localMember) {
        //skip for segmented global schedule
    }
    public void atMidnight(){
        /** not used for onDemand schedule
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
        }));**/
        ArrayList<Long> removingList = new ArrayList<>();
        tournamentIndex.forEach((k,v)->{
            if(v.status()== Tournament.Status.ENDED) removingList.add(k);
        });
        removingList.forEach(r->{
            tournamentIndex.remove(r);
        });
    }
    private void scheduleTournament(){
        LocalDateTime _current = LocalDateTime.now();
        TournamentScheduleStatusQuery query = new TournamentScheduleStatusQuery(this.gameCluster.distributionId());
        dataStore.list(query).forEach(ts->{
            TournamentSchedule schedule = this.tournamentSchedule(ts.distributionId());
            if(schedule!=null){
                if(schedule.startTime().getDayOfYear() == _current.plusDays(1).getDayOfYear()){
                    scheduleStore.queueOffer(BufferUtil.fromLong(ts.distributionId()));
                }
            }
        });
    }

    private long sortingTimer(){
        return snapshotTimerInterval.get()*60*1000+3*60000;//add 3 minute buffer
    }
    void startTournament(TournamentManager tournament){
        logger.warn("Tournament start : "+tournament.distributionId()+" : "+tournament.global());
        tournament.setup(this);
        if(tournament.toClosingTime()>=sortingTimer()){
            tournament.nextSortingTime = LocalDateTime.now().plusMinutes(snapshotTimerInterval.get());
            logger.warn("Next sorting time : "+tournament.nextSortingTime);
            tournament.pendingSchedule = this.serviceContext.schedule(new TournamentSnapshotMonitor(tournament,this));
        }else{
            tournament.pendingSchedule = this.serviceContext.schedule(new TournamentCloseMonitor(tournament,this));
        }
        logger.warn(tournament.toString());
        if(this.application==null) return;
        tournament.loadPrizes(this.applicationPreSetup,this.application);
        listeners.forEach(l->l.tournamentStarted(tournament));
    }
    void sortTournament(TournamentManager tournament){
        tournament.snapshot();
        if(tournament.toClosingTime()>=sortingTimer()){
            tournament.nextSortingTime = LocalDateTime.now().plusMinutes(snapshotTimerInterval.get());
            logger.warn("Next sorting time : "+tournament.nextSortingTime);
            tournament.pendingSchedule = this.serviceContext.schedule(new TournamentSnapshotMonitor(tournament,this));
        }else{
            tournament.pendingSchedule = this.serviceContext.schedule(new TournamentCloseMonitor(tournament,this));
        }
    }
    void closeTournament(TournamentManager tournament){
        logger.warn("Tournament Close : "+tournament.distributionId()+" : "+tournament.global());
        this.listeners.forEach(l->l.tournamentClosed(tournament));
        TournamentScheduleStatus status = loadStatus(tournament.scheduleId());
        byte[] lockKey = status.key().asBinary();
        try {
            scheduleStore.mapLock(lockKey);
            status.update(Tournament.Status.CLOSED);
            tournament.close();
            this.serviceContext.schedule(new TournamentEndMonitor(tournament, this));
        }finally {
            scheduleStore.mapUnlock(lockKey);
        }
    }
    void endTournament(TournamentManager tournament){
        logger.warn("Tournament End : "+tournament.distributionId()+" : "+tournament.global());
        this.distributionItemService.onReleaseItem(gameServiceName, name(), "TournamentSchedule", tournament.distributionKey());
        TournamentScheduleStatus status = loadStatus(tournament.scheduleId());
        byte[] lockKey = status.key().asBinary();
        try {
            scheduleStore.mapLock(lockKey);
            tournament.end();
            clearTournament(tournament);
            tournamentIndex.remove(tournament.distributionId());
        }
        catch (Exception ex){
            logger.error("Error on end tournament : "+tournament.distributionId()+" : ",ex);
        }
        finally {
            scheduleStore.mapUnlock(lockKey);
        }

    }

    ///schedule and launch
    @Override
    public <T extends Configurable> void register(T t) {
        if(!t.configurationCategory().equals("TournamentSchedule")) throw new RuntimeException(t.configurationCategory()+" cannot be registered");
        TournamentSchedule schedule = new TournamentSchedule((ConfigurableObject) t);
        TournamentScheduleStatus status = schedule.status();
        status.ownerKey(SnowflakeKey.from(this.gameCluster.distributionId()));
        dataStore.createIfAbsent(status,true);
        byte[] lockKey = status.key().asBinary();
        try{
            scheduleStore.mapLock(lockKey);
            if(status.status != Tournament.Status.PENDING) throw new RuntimeException("schedule is running on tournament ["+status.tournamentId+"]");
            validateSchedule(schedule);
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
        if(!t.configurationCategory().equals("TournamentSchedule")) throw new RuntimeException(t.configurationCategory()+" cannot be released");
        TournamentScheduleStatus status = loadStatus(t.distributionId());
        if(status == null) throw new RuntimeException("no schedule status installed");
        byte[] lockKey = status.key().asBinary();
        try {
            scheduleStore.mapLock(lockKey);
            if (status.status == Tournament.Status.PENDING) { //cancel schedule
                t.released();
                return;
            }
            if(status.status == Tournament.Status.STARTING) throw new RuntimeException("Tournament cannot be canceled during starting.");
            distributionTournamentService.onEndTournament(gameServiceName, status.tournamentId);
            //t.released();
        }finally {
            scheduleStore.mapUnlock(lockKey);
        }
    }

    @Override
    public boolean onItemRegistered(String category, String itemId) {
        TournamentManager tournament = new TournamentManager();
        tournament.distributionKey(itemId);
        if (!this.dataStore.load(tournament)) {
            logger.warn("Tournament cannot be loaded ["+itemId+"]");
            return false;
        }
        tournament.dataStore(dataStore);
        launch(tournament);
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
        TournamentScheduleStatus status = loadStatus(schedule.distributionId());
        TournamentManager tournament = new TournamentManager(schedule);
        tournament.ownerKey(SnowflakeKey.from(schedule.distributionId()));
        tournament.dataStore(dataStore);
        if(!dataStore.create(tournament)){
            throw new RuntimeException("Failed to create tournament manager");
        }
        RecentlyTournamentList list = RecentlyTournamentList.lookup(recentlyTournamentIndex,schedule.type(),recentlyTournamentListSize);
        list.push(tournament);
        list.update();
        if(schedule.global()){
            for(int i=0;i<schedule.segmentsPerSchedule();i++){
                tournament.tournamentServiceProvider = this;
                tournament.createGlobalInstance();
            }
        }
        status.tournamentId = tournament.distributionId();
        status.update(Tournament.Status.STARTING);

        this.serviceContext.schedule(new ScheduleRunner(SCHEDULE_RUNNER_DELAY,()->{
            TournamentScheduleStatus started = loadStatus(tournament.scheduleId());
            if(started==null) {
                logger.warn("Tournament schedule status missed ["+schedule.distributionId()+"]");
                return;
            }
            byte[] lockKey = started.key().asBinary();
            try{
                this.scheduleStore.mapLock(lockKey);
                started.update(Tournament.Status.STARTED);
                this.distributionItemService.onRegisterItem(gameServiceName,name(),"TournamentSchedule",tournament.distributionKey());
            }finally {
                this.scheduleStore.mapUnlock(lockKey);
            }
        }));
    }
    private void launch(TournamentManager tournament){
        this.tournamentIndex.put(tournament.distributionId(),tournament);
        logger.warn("Tournament ["+tournament.distributionId()+"] is scheduling to start at ["+tournament.startTime()+"]");
        tournament.pendingSchedule = this.serviceContext.schedule(new TournamentStartMonitor(tournament,this));
    }



    private void clearTournament(TournamentManager tournament){
        TournamentScheduleStatus status = this.loadStatus(tournament.scheduleId());
        status.update(Tournament.Status.PENDING);
        ConfigurableObject schedule = this.tournamentSchedule(tournament.scheduleId()).configurableObject;
        schedule.released();
    }
    private TournamentSchedule tournamentSchedule(long scheduleId){
        ConfigurableObject schedule = new ConfigurableObject();
        schedule.distributionId(scheduleId);
        return applicationPreSetup.load(application,schedule) ? new TournamentSchedule(schedule) : null;
    }

    long nextInstanceId(){
        return serviceContext.distributionId();
    }


    ScheduledFuture<?> schedule(SchedulingTask task){
        return serviceContext.schedule(task);
    }

    void updateTournamentRegister(TournamentRegister register){
        this.dataStore.update(register);
    }
    private TournamentScheduleStatus loadStatus(long scheduleId){
        TournamentScheduleStatus status = new TournamentScheduleStatus();
        status.distributionId(scheduleId);
        status.dataStore(dataStore);
        return dataStore.load(status)?status:null;
    }

    //distributed operation callbacks
    public TournamentRegisterStatus onTournamentRegistered(long tournamentId,int slot){
        TournamentManager tournamentManager = this.tournamentIndex.get(tournamentId);
        return tournamentManager.onRegister(slot);
    }
    public long onTournamentSegmentEntered(long tournamentId,long segmentInstanceId,long systemId){
        TournamentManager tournamentManager = this.tournamentIndex.get(tournamentId);
        return tournamentManager.onEnterSegment(systemId,segmentInstanceId);
    }
    public double onTournamentSegmentScored(long tournamentId,long instanceId,long entryId, long systemId, double credit,double delta){
        TournamentManager tournamentManager = this.tournamentIndex.get(tournamentId);
        return tournamentManager.onScoreSegment(systemId,instanceId,entryId,credit,delta);
    }
    public Tournament.Instance onTournamentEntered(long tournamentId,long instanceId,long systemId){
        TournamentManager tournamentManager = this.tournamentIndex.get(tournamentId);
        return tournamentManager.onEnter(systemId,instanceId);
    }
    public double onTournamentScored(long tournamentId,long instanceId, long systemId, double credit,double delta){
        TournamentManager tournamentManager = this.tournamentIndex.get(tournamentId);
        return tournamentManager.onScore(systemId,instanceId,credit,delta);
    }

    public byte[] onTournamentRaceBoardListed(long tournamentId,long instanceId){
        TournamentManager tournamentManager = (TournamentManager)this.tournament(tournamentId);
        return tournamentManager.onRaceBoard(instanceId).toBinary();
    }
    public byte[] onTournamentMyRaceBoardListed(long tournamentId,long instanceId,long entryId,long systemId){
        TournamentManager tournamentManager = (TournamentManager)this.tournament(tournamentId);
        return tournamentManager.onMyRaceBoard(instanceId,entryId,systemId).toBinary();
    }

    public void onTournamentEnded(long tournamentId){
        logger.warn("End tournament forcefully  : "+tournamentId);
        TournamentManager tournament = tournamentIndex.get(tournamentId);
        if(tournament == null ) {
            logger.warn("No tournament loaded : "+tournamentId);
            return;
        }
        tournament.pendingSchedule.cancel(true);
        this.listeners.forEach(l->l.tournamentClosed(tournament));
        this.serviceContext.schedule(new ScheduleRunner(SCHEDULE_RUNNER_DELAY,()->{
            endTournament(tournament);
        }));
    }
    //end of distributed callbacks

    private void validateSchedule(TournamentSchedule schedule){
        //common
        if(TimeUtil.durationUTCInHours(schedule.startTime(),schedule.endTime()) < minDurationHoursPerSchedule) throw new RuntimeException("min hours per schedule less than ["+minDurationHoursPerSchedule+"]");
        if(schedule.name()==null || schedule.name().trim().length() < 4) throw new RuntimeException("Name cannot be null or less 4 chars");
        if(schedule.type()==null || schedule.type().trim().length() < 4) throw new RuntimeException("Type cannot be null or less 4 chars");

        //global schedule
        if(schedule.global() && schedule.segmentsPerSchedule()<=0) throw new RuntimeException("global segments per schedule must be at least 1 or more");
        if(schedule.global() && schedule.schedule() != Tournament.Schedule.ON_DEMAND_SCHEDULE) throw new RuntimeException("global schedule only support on demand");
        if(schedule.global() && schedule.maxEntriesPerInstance()<=0){
            schedule.maxEntriesPerInstance(topRaceBoardSize);
        }
        //none global schedule
        if(!schedule.global() && schedule.maxEntriesPerInstance()<=0) throw new RuntimeException("max entries per instance  must be at least 1 or more");
        if(!schedule.global() && schedule.durationMinutesPerInstance() < minDurationMinutesPerInstance) throw new RuntimeException("min minutes per instance less than ["+minDurationMinutesPerInstance+"]");

    }

    //private RecentlyTournamentList lookupRecentlyTournamentList(String type){
        //return typedTournamentIndex.computeIfAbsent(type,key->{
            //RecentlyTournamentList loaded = RecentlyTournamentList.lookup(this.recentlyTournamentIndex,this.gameCluster.distributionId(),type,this.recentlyTournamentListSize);
            //return loaded;
        //});
    //}

}
