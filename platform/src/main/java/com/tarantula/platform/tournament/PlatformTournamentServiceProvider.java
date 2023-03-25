package com.tarantula.platform.tournament;

import com.icodesoftware.*;
import com.icodesoftware.service.*;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.IndexSet;
import com.tarantula.platform.ScheduleRunner;
import com.tarantula.platform.inventory.PlatformInventoryServiceProvider;
import com.tarantula.platform.item.ConfigurableObject;
import com.tarantula.platform.item.DistributionItemService;
import com.tarantula.platform.service.ApplicationPreSetup;
import com.tarantula.platform.item.ItemDistributionCallback;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class PlatformTournamentServiceProvider implements TournamentServiceProvider, ReloadListener, ConfigurationServiceProvider, ItemDistributionCallback {

    private static final String CONFIG = "game-tournament-settings";

    public static final String NAME = "tournament";

    private final static String TOURNAMENT_LOOKUP_INDEX = "tournament";

    private final static String TOURNAMENT_SCHEDULE_LOOKUP_INDEX = "schedule";

    final static long SCHEDULE_RUNNER_DELAY = 500;

    TarantulaLogger logger;

    ServiceContext serviceContext;

    DistributionTournamentService distributionTournamentService;

    private DistributionItemService distributionItemService;
    final String gameServiceName;
    private DataStore dataStore;
    private CopyOnWriteArrayList<Tournament.Listener> listeners = new CopyOnWriteArrayList<>();

    private ConcurrentHashMap<String,TournamentManager> tournamentIndex = new ConcurrentHashMap<>();


    private IndexSet lookupTournamentKey;
    private IndexSet lookupScheduleKey;

    int concurrentInstanceSize = 8;
    int minDurationHoursPerSchedule = 1;
    int minDurationMinutesPerInstance =  5;
    int endBufferTimeMinutes = 3;
    double scoreCredits;
    int clusterLockTimeoutSeconds = 5;
    int instanceIdPollingTimeoutSeconds = 3;
    int instanceIdPollingRetries =3;
    int pendingInstancePoolSizePerSchedule = 100;
    int maxPlayerHistoryRecords = 10;

    private String reloadKey;
    private final GameCluster gameCluster;
    private ApplicationPreSetup applicationPreSetup;
    private Descriptor application;
    PlatformInventoryServiceProvider inventoryServiceProvider;

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


    @Override
    public boolean available(String tournamentId) {
        return tournamentIndex.get(tournamentId) != null;
    }

    @Override
    public Tournament.Instance enter(String tournamentId, String systemId) {
        TournamentManager index = tournamentIndex.get(tournamentId);
        byte[] pendingId = null;
        for(int retry = 0;retry < this.instanceIdPollingRetries;retry++){
            pendingId = index.pollInstanceId();
            if(pendingId != null) break;
        }
        if(pendingId == null) return null;
        String instanceId = new String(pendingId);
        Tournament.Instance instance = this.distributionTournamentService.onEnterTournament(gameServiceName,tournamentId,instanceId,systemId);
        instance.distributionKey(instanceId);
        return instance;
    }

    @Override
    public Tournament.Entry score(String tournamentId,String instanceId, String systemId, double credit,double delta) {
        Tournament.Entry _e = this.distributionTournamentService.onScoreTournament(gameServiceName,tournamentId,instanceId,systemId,credit,delta);
        //logger.warn(_e.toJson().toString());
        return _e;
    }

    public void finish(String tournamentId,String instanceId, String systemId){
        this.distributionTournamentService.onFinishTournament(gameServiceName,tournamentId,instanceId,systemId);
        //Tournament.RaceBoard board = this.distributionTournamentService.onListTournament(gameServiceName,tournamentId,instanceId);
        //logger.warn(board.toJson().toString());
    }
    @Override
    public Tournament.RaceBoard list(String tournamentId,String instanceId) {
        Tournament.RaceBoard ins = this.distributionTournamentService.onListTournament(gameServiceName,tournamentId,instanceId);
        Collections.sort(ins.list(),new TournamentEntryComparator());
        return ins;
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
        this.concurrentInstanceSize = ((Number)configuration.property("concurrentInstanceSize")).intValue();
        this.minDurationHoursPerSchedule = ((Number)configuration.property("minDurationHoursPerSchedule")).intValue();
        this.minDurationMinutesPerInstance = ((Number)configuration.property("minDurationMinutesPerInstance")).intValue();
        this.endBufferTimeMinutes = ((Number)configuration.property("endBufferTimeMinutes")).intValue();
        this.scoreCredits = ((Number)configuration.property("scoreCredits")).doubleValue();
        this.maxPlayerHistoryRecords = ((Number)configuration.property("maxPlayerHistoryRecords")).intValue();
        this.clusterLockTimeoutSeconds = ((Number)configuration.property("clusterLockTimeoutSeconds")).intValue();
        this.instanceIdPollingTimeoutSeconds = ((Number)configuration.property("instanceIdPollingTimeoutSeconds")).intValue();
        this.instanceIdPollingRetries = ((Number)configuration.property("instanceIdPollingRetries")).intValue();
        this.pendingInstancePoolSizePerSchedule = ((Number)configuration.property("pendingInstancePoolSizePerSchedule")).intValue();
        this.lookupTournamentKey = new IndexSet(TOURNAMENT_LOOKUP_INDEX);
        this.lookupTournamentKey.distributionKey(serviceContext.node().nodeId());
        this.lookupScheduleKey = new IndexSet(TOURNAMENT_SCHEDULE_LOOKUP_INDEX);
        this.lookupScheduleKey.distributionKey(serviceContext.node().nodeId());
        this.dataStore = applicationPreSetup.dataStore(gameCluster,name());
        this.dataStore.createIfAbsent(this.lookupTournamentKey,true);
        this.dataStore.createIfAbsent(this.lookupScheduleKey,true);
        this.lookupTournamentKey.dataStore(this.dataStore);
        this.lookupScheduleKey.dataStore(this.dataStore);
        this.logger = this.serviceContext.logger(PlatformTournamentServiceProvider.class);
        this.reloadKey = this.serviceContext.clusterProvider().registerReloadListener(this);
        this.distributionTournamentService = this.serviceContext.clusterProvider().serviceProvider(DistributionTournamentService.NAME);
        this.distributionItemService = this.serviceContext.clusterProvider().serviceProvider(DistributionItemService.NAME);
        this.scheduleStore = this.serviceContext.clusterProvider().clusterStore(ClusterProvider.ClusterStore.SMALL,gameCluster.typeId()+"."+NAME);
    }

    @Override
    public void start() throws Exception {
        ArrayList<String> removed = new ArrayList();
        lookupTournamentKey.keySet().forEach((k)->{
            TournamentManager tournament = new TournamentManager();
            tournament.distributionKey(k);
            if(this.dataStore.load(tournament) && tournament.status() != Tournament.Status.ENDED){
                TournamentScheduleStatus status = new TournamentScheduleStatus();
                status.distributionKey(tournament.index());
                this.dataStore.load(status);
                byte[] lockKey = k.getBytes();
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
            else{
                logger.warn("Tournament removed->"+k);
                removed.add(k);
            }
        });
        removed.forEach((r)->{
            lookupTournamentKey.removeKey(r);
        });
        this.lookupTournamentKey.update();
        this.logger.warn("Tournament service provider started with concurrent tournament pool size->["+concurrentInstanceSize+"][ on game service ["+gameServiceName+"]["+gameCluster.name()+"]");
    }

    @Override
    public void shutdown() throws Exception {
        this.logger.warn("distributed tournament shutdown");
        this.serviceContext.clusterProvider().unregisterReloadListener(reloadKey);
    }

    public List<Tournament.History> playerHistory(String systemId){
        ArrayList<Tournament.History> list = new ArrayList<>();
        IndexSet indexSet = new IndexSet(Tournament.HISTORY_LABEL);
        indexSet.distributionKey(systemId);
        this.dataStore.createIfAbsent(indexSet,true);
        indexSet.keySet().forEach((k)->{
            TournamentHistory h = new TournamentHistory();
            h.distributionKey(k);
            if(dataStore.load(h)){
                list.add(h);
            }
        });
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
                    TournamentSchedule schedule = this.tournamentSchedule(new String(pendingSchedule));
                    if(schedule!=null && !schedule.configurableObject.disabled()){
                        registerTournament(schedule);
                    }
                }
            }while(pendingSchedule!=null);
            lookupScheduleKey.reload();
            scheduleTournament();
            ArrayList<String> removed = new ArrayList();
            this.lookupTournamentKey.reload();
            this.lookupTournamentKey.keySet().forEach(k->{
                TournamentManager tournamentManager = new TournamentManager();
                tournamentManager.distributionKey(k);
                this.dataStore.load(tournamentManager);
                if(tournamentManager.status() == Tournament.Status.ENDED) removed.add(k);
            });
            removed.forEach((r)->{
                lookupTournamentKey.removeKey(r);
            });
            lookupTournamentKey.update();}
        ));
    }
    private void scheduleTournament(){
        LocalDateTime _current = LocalDateTime.now();
        lookupScheduleKey.keySet().forEach(k->{
            TournamentSchedule schedule = this.tournamentSchedule(k);
            if(schedule!=null){
                if(schedule.startTime().getDayOfYear() == _current.plusDays(1).getDayOfYear()){
                    scheduleStore.queueOffer(k.getBytes());
                }
            }
            else{
                lookupScheduleKey.removeKey(k);
            }
        });
        this.lookupScheduleKey.update();
    }


    void closeTournament(TournamentManager tournament){
        this.listeners.forEach(l->l.tournamentClosed(tournament));
        byte[] lockKey = tournament.distributionKey().getBytes();
        try {
            scheduleStore.mapLock(lockKey);
            tournament.close();
            this.serviceContext.schedule(new TournamentEndMonitor(tournament, this));
        }finally {
            scheduleStore.mapUnlock(lockKey);
        }
    }
    void endTournament(TournamentManager tournament){
        byte[] lockKey = tournament.distributionKey().getBytes();
        try {
            scheduleStore.mapLock(lockKey);
            tournament.end();
            if(scheduleStore.mapExists(lockKey)) {
                scheduleStore.mapRemove(lockKey);
                clearTournament(tournament);
                this.distributionItemService.onReleaseItem(gameServiceName, name(), "TournamentSchedule", tournament.distributionKey());
            }
            lookupTournamentKey.removeKey(tournament.distributionKey());
            lookupTournamentKey.update();
        }
        finally {
            scheduleStore.mapUnlock(lockKey);
        }
    }

    ///schedule and launch
    @Override
    public <T extends Configurable> void register(T t) {
        if(!t.configurationCategory().equals("TournamentSchedule")) throw new RuntimeException(t.configurationCategory()+" cannot be registered");
        byte[] lockKey = t.distributionKey().getBytes();
        try{
            scheduleStore.mapLock(lockKey);
            TournamentScheduleStatus status = new TournamentScheduleStatus();
            status.distributionKey(t.distributionKey());
            dataStore.createIfAbsent(status,true);
            if(status.status != Tournament.Status.PENDING ) throw new RuntimeException("schedule is running on tournament ["+status.index()+"]");
            TournamentSchedule schedule = new TournamentSchedule((ConfigurableObject) t);
            if(schedule.durationHoursPerSchedule() < minDurationHoursPerSchedule) throw new RuntimeException("min hours per schedule less than ["+minDurationHoursPerSchedule+"]");
            if(schedule.durationMinutesPerInstance() < minDurationMinutesPerInstance) throw new RuntimeException("min minutes per instance less than ["+minDurationMinutesPerInstance+"]");
            switch (schedule.schedule()){
                case Tournament.DAILY_SCHEDULE:
                case Tournament.WEEKLY_SCHEDULE:
                case Tournament.MONTHLY_SCHEDULE:
                    registerSchedule(schedule);
                    break;
                case Tournament.ON_DEMAND_SCHEDULE:
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
            status.distributionKey(t.distributionKey());
            dataStore.createIfAbsent(status, true);
            if (status.index() == null) { //cancel schedule
                lookupScheduleKey.removeKey(t.distributionKey());
                lookupScheduleKey.update();
                t.released();
                return;
            }
            if(status.status == Tournament.Status.STARTING) throw new RuntimeException("Tournament cannot be canceled during starting.");
            //forcefully end tournament
            distributionTournamentService.onEndTournament(gameServiceName, status.index());
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
        lookupScheduleKey.addKey(schedule.distributionKey());
        lookupScheduleKey.update();
        if(schedule.startTime().getDayOfYear() == _current.plusDays(1).getDayOfYear()){
            this.scheduleStore.queueOffer(schedule.distributionKey().getBytes());
        }
    }
    private void registerTournament(TournamentSchedule schedule){
        TournamentScheduleStatus status = schedule.status();
        this.dataStore.load(status);
        TournamentManager tournament = new TournamentManager(schedule);
        tournament.dataStore(dataStore);
        dataStore.create(tournament);
        status.index(tournament.distributionKey());
        status.status = Tournament.Status.STARTING;
        dataStore.update(status);
        lookupTournamentKey.addKey(tournament.distributionKey());
        lookupTournamentKey.update();
        lookupScheduleKey.removeKey(schedule.distributionKey());
        lookupScheduleKey.update();
        logger.warn(tournament.toString());
        this.serviceContext.schedule(new ScheduleRunner(SCHEDULE_RUNNER_DELAY,()->{
            byte[] lockKey = tournament.distributionKey().getBytes();
            try{
                this.scheduleStore.mapLock(lockKey);
                status.status = Tournament.Status.STARTED;
                this.scheduleStore.mapSet(lockKey,status.toBinary());
                this.distributionItemService.onRegisterItem(gameServiceName,name(),"TournamentSchedule",tournament.distributionKey());
                dataStore.update(status);
            }finally {
                this.scheduleStore.mapUnlock(lockKey);
            }
        }));
    }
    private void launch(TournamentManager tournament){
        this.tournamentIndex.put(tournament.distributionKey(),tournament);
        tournament.setup(this);
        tournament.pendingSchedule = this.serviceContext.schedule(new TournamentCloseMonitor(tournament,this));
        logger.warn(tournament.toString());
        if(this.application==null) return;
        tournament.loadPrizes(this.applicationPreSetup,this.application);
    }

    private void clearTournament(TournamentManager tournament){
        TournamentScheduleStatus status = new TournamentScheduleStatus();
        status.distributionKey(tournament.index());
        this.dataStore.load(status);
        status.index(null);
        status.status = Tournament.Status.PENDING;
        dataStore.update(status);
        ConfigurableObject schedule = this.tournamentSchedule(tournament.index()).configurableObject;
        schedule.released();
    }
    private TournamentSchedule tournamentSchedule(String scheduleId){
        ConfigurableObject schedule = new ConfigurableObject();
        schedule.distributionKey(scheduleId);
        return applicationPreSetup.load(application,schedule) ? new TournamentSchedule(schedule) : null;
    }

    //distributed operation callbacks
    public Tournament.Instance onTournamentEntered(String tournamentId,String instanceId,String systemId){
        TournamentManager tournamentManager = this.tournamentIndex.get(tournamentId);
        TournamentInstance _ins = tournamentManager.lookup(instanceId);
        if(_ins.enter(systemId) == _ins.maxEntries()) tournamentManager.closeTournamentInstanceWithFullyJoined(_ins);
        return _ins;
    }
    public Tournament.Entry onTournamentScored(String tournamentId,String instanceId, String systemId, double credit,double delta){
        TournamentManager tournamentManager = this.tournamentIndex.get(tournamentId);
        TournamentInstance _ins = tournamentManager.lookup(instanceId);
        if(_ins==null) return new TournamentEntry();
        Tournament.Entry[] score={null};
        if(_ins.update(systemId,(e)->{
            e.score(credit,delta);
            score[0]=e;
            return e.finished();
        })) tournamentManager.endTournamentInstanceWithFullyFinished(_ins);
        return score[0];
    }
    public Tournament.RaceBoard onTournamentListed(String tournamentId,String instanceId){
        TournamentManager tournamentManager = this.tournamentIndex.get(tournamentId);
        Tournament.Instance _ins = tournamentManager.lookup(instanceId);
        if(_ins == null) return new TournamentRaceBoard();
        return _ins.raceBoard();
    }
    public void onTournamentFinished(String tournamentId,String instanceId,String systemId){
        TournamentManager tournamentManager = this.tournamentIndex.get(tournamentId);
        TournamentInstance _ins = tournamentManager.lookup(instanceId);
        if(_ins==null) return;
        if(_ins.update(systemId,e->{
            e.finish();
            return e.finished();
        })) tournamentManager.endTournamentInstanceWithFullyFinished(_ins);
    }

    public void onTournamentSynced(String tournamentId,String instanceId){
        TournamentManager tournamentManager = this.tournamentIndex.get(tournamentId);
        TournamentInstance synced = tournamentManager.lookup(instanceId);
        logger.warn("SYNC Instance : "+synced);
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
