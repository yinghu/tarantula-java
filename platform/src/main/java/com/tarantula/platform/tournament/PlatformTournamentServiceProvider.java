package com.tarantula.platform.tournament;

import com.icodesoftware.*;
import com.icodesoftware.service.*;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.IndexSet;
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

    TarantulaLogger logger;

    ServiceContext serviceContext;

    DistributionTournamentService distributionTournamentService;

    private DistributionItemService distributionItemService;
    final String gameServiceName;
    private DataStore dataStore;
    private CopyOnWriteArrayList<Tournament.Listener> listeners = new CopyOnWriteArrayList<>();

    private ConcurrentHashMap<String,TournamentManager> tournamentIndex = new ConcurrentHashMap<>();

    ConcurrentHashMap<String, TournamentInstance> instanceIndex = new ConcurrentHashMap<>();

    private IndexSet lookupTournamentKey;
    private IndexSet lookupScheduleKey;

    int concurrentInstanceSize = 8;
    int minDurationHoursPerSchedule = 1;
    int minDurationMinutesPerInstance =  5;
    int endBufferTimeMinutes = 3;
    int clusterLockTimeoutSeconds = 5;

    int instanceIdPollingRetries =3;

    private String reloadKey;
    private final GameCluster gameCluster;
    private ApplicationPreSetup applicationPreSetup;
    private Descriptor application;
    private PlatformInventoryServiceProvider inventoryServiceProvider;

    private ClusterProvider.ClusterStore clusterStore;

    public PlatformTournamentServiceProvider(GameServiceProvider gameServiceProvider){
        this.gameCluster = gameServiceProvider.gameCluster();
        this.gameServiceName = this.gameCluster.serviceType();
        this.inventoryServiceProvider = gameServiceProvider.inventoryServiceProvider();
    }

    @Override
    public void registerTournamentListener(Tournament.Listener listener) {
        listeners.add(listener);
    }
    @Override
    public String registerConfigurableListener(Descriptor descriptor, Configurable.Listener listener) {
        this.application = descriptor;
        logger.warn("register application ->"+application.category());
        return null;
    }


    @Override
    public boolean available(String tournamentId) {
        return tournamentIndex.get(tournamentId)!=null;
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
    public Tournament.Entry score(String instanceId, String systemId, double delta) {
        Tournament.Entry _e = this.distributionTournamentService.onScoreTournament(gameServiceName,instanceId,systemId,delta);
        return _e;
    }

    public void finish(String instanceId, String systemId){
        this.distributionTournamentService.onFinishTournament(gameServiceName,instanceId,systemId);
    }
    @Override
    public Tournament.RaceBoard list(String instanceId) {
        Tournament.RaceBoard ins = this.distributionTournamentService.onListTournament(gameServiceName,instanceId);
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
        this.clusterLockTimeoutSeconds = ((Number)configuration.property("clusterLockTimeoutSeconds")).intValue();
        this.instanceIdPollingRetries = ((Number)configuration.property("instanceIdPollingRetries")).intValue();
        this.lookupTournamentKey = new IndexSet(GameCluster.TOURNAMENT_LOOKUP_INDEX);
        this.lookupTournamentKey.distributionKey(gameCluster.distributionKey());
        this.lookupScheduleKey = new IndexSet(GameCluster.TOURNAMENT_SCHEDULE_LOOKUP_INDEX);
        this.lookupScheduleKey.distributionKey(gameCluster.distributionKey());
        this.dataStore = applicationPreSetup.dataStore(gameCluster,name());
        this.dataStore.createIfAbsent(this.lookupTournamentKey,true);
        this.dataStore.createIfAbsent(this.lookupScheduleKey,true);
        this.lookupTournamentKey.dataStore(this.dataStore);
        this.lookupScheduleKey.dataStore(this.dataStore);
        this.logger = this.serviceContext.logger(PlatformTournamentServiceProvider.class);
        this.reloadKey = this.serviceContext.clusterProvider().registerReloadListener(this);
        this.distributionTournamentService = this.serviceContext.clusterProvider().serviceProvider(DistributionTournamentService.NAME);
        this.distributionItemService = this.serviceContext.clusterProvider().serviceProvider(DistributionItemService.NAME);
        this.clusterStore = this.serviceContext.clusterProvider().clusterStore(ClusterProvider.ClusterStore.SMALL,gameCluster.typeId()+"."+NAME);
    }

    @Override
    public void start() throws Exception {
        ArrayList<String> removed = new ArrayList();
        lookupTournamentKey.keySet().forEach((k)->{
            if(!loadTournamentHeader(k)){
                removed.add(k);
            }
        });
        removed.forEach((r)->{
            lookupTournamentKey.removeKey(r);
        });
        this.lookupTournamentKey.update();
        this.serviceContext.schedule(new TournamentMidnightTask(this));
        this.logger.warn("Tournament service provider started with concurrent tournament pool size->["+concurrentInstanceSize+"][ on game service ["+gameServiceName+"]["+gameCluster.name()+"]");
    }

    @Override
    public void shutdown() throws Exception {
        this.logger.warn("distributed tournament shutdown");
        this.serviceContext.clusterProvider().unregisterReloadListener(reloadKey);
    }

    public Tournament tournament(String tournamentId){//schedule node
        TournamentManager tournament = tournamentIndex.get(tournamentId);
        return tournament;
    }
    public Tournament.Instance instance(String tournamentId,String instanceId){//instance node
        TournamentManager tournament = this.tournamentIndex.get(tournamentId);
        return tournament.lookup(instanceId);
    }
    public Tournament.Instance instance(String instanceId){//instance node
        TournamentInstance tournament = this.instanceIndex.get(instanceId);
        return tournament;
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
        serviceContext.schedule(new TournamentMidnightTask(this));
    }
    void midnightCheck(){
        //midnight close/launch daily/weekly/monthly tournaments
        this.lookupScheduleKey.keySet().forEach(k->{
            clusterStore.mapLock(k.getBytes());
            TournamentSchedule schedule = new TournamentSchedule();
            schedule.distributionKey(k);
            if(applicationPreSetup.load(application,schedule)){
                if(schedule.startTime().getDayOfYear() == LocalDateTime.now().getDayOfYear()){
                    //if(distributionTournamentService.localManaged(k).localManaged){
                        TournamentManager tournament = createTournament(schedule);
                        this.serviceContext.schedule(new TournamentRegisterTask(tournament,this));
                    //}
                }
            }
            clusterStore.mapUnlock(k.getBytes());
        });
    }
    void onTournamentRegister(Tournament tournamentHeader){
        this.distributionItemService.onRegisterItem(gameServiceName,name(),"TournamentSchedule",tournamentHeader.distributionKey());
    }
    void onTournamentClose(TournamentManager tournamentHeader){
        byte[] lockKey = tournamentHeader.index().getBytes();
        try {
            clusterStore.mapLock(lockKey);
            this.distributionTournamentService.onCloseTournament(gameServiceName, tournamentHeader.distributionKey());
            this.serviceContext.schedule(new TournamentEndMonitor(tournamentHeader, this));
        }finally {
            clusterStore.mapUnlock(lockKey);
        }
    }
    void onTournamentEnd(TournamentManager tournamentHeader){
        byte[] lockKey = tournamentHeader.index().getBytes();
        try {
            clusterStore.mapLock(lockKey);
            endTournament(tournamentHeader);
            this.distributionItemService.onReleaseItem(gameServiceName, name(), "TournamentSchedule", tournamentHeader.distributionKey());
        }
        finally {
            clusterStore.mapUnlock(lockKey);
        }
    }

    void onPrize(String systemId,TournamentPrize prize){
        inventoryServiceProvider.redeem(systemId,prize);
    }

    Map<Integer,TournamentPrize> prize(String scheduleId){
        TournamentSchedule schedule = new TournamentSchedule();
        schedule.distributionKey(scheduleId);
        if(!this.applicationPreSetup.load(application,schedule)) return new HashMap<>();
        schedule.setup();
        Map<Integer,TournamentPrize> _prizes = new HashMap<>();
        schedule.list().forEach(c-> _prizes.put(c.rank(),c));
        return _prizes;
    }

    ///schedule and launch
    @Override
    public <T extends Configurable> void register(T t) {
        byte[] lockKey = t.distributionKey().getBytes();
        try{
            clusterStore.mapLock(lockKey);
            if(!t.configurationCategory().equals("TournamentSchedule")) throw new RuntimeException(t.configurationCategory()+" cannot be registered");
            TournamentScheduleStatus status = new TournamentScheduleStatus();
            status.distributionKey(t.distributionKey());
            dataStore.createIfAbsent(status,true);
            if(status.index() != null) throw new RuntimeException("schedule is running on tournament ["+status.index()+"]");
            TournamentSchedule schedule = new TournamentSchedule((ConfigurableObject) t);
            if(schedule.durationHoursPerSchedule()<minDurationHoursPerSchedule) throw new RuntimeException("min hours per schedule less than ["+minDurationHoursPerSchedule+"]");
            if(schedule.durationMinutesPerInstance()<minDurationMinutesPerInstance) throw new RuntimeException("min minutes per instance less than ["+minDurationMinutesPerInstance+"]");
            switch (schedule.schedule()){
                case Tournament.DAILY_SCHEDULE:
                case Tournament.WEEKLY_SCHEDULE:
                case Tournament.MONTHLY_SCHEDULE:
                    LocalDateTime _current = LocalDateTime.now();
                    if(TimeUtil.expired(schedule.startTime())
                            || (schedule.startTime().getYear() ==_current.getYear() && schedule.startTime().getDayOfYear() ==_current.getDayOfYear())) throw new RuntimeException("start time already expired on daily midnight launch");
                    createSchedule(schedule);
                    break;
                case Tournament.ON_DEMAND_SCHEDULE:
                    TournamentManager tournament = createTournament(schedule);
                    this.serviceContext.schedule(new TournamentRegisterTask(tournament,this));
                    break;
                default:
                    throw new RuntimeException("schedule plan not supported ["+schedule.schedule()+"]");
            }
            t.registered();
        }finally {
            clusterStore.mapUnlock(lockKey);
        }
    }
    @Override
    public <T extends Configurable> void release(T t) {
        byte[] lockKey = t.distributionKey().getBytes();
        try {
            clusterStore.mapLock(lockKey);
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
            distributionItemService.onReleaseItem(gameServiceName, name(), t.configurationTypeId(),status.index());
        }finally {
            clusterStore.mapUnlock(lockKey);
        }
    }

    @Override
    public boolean onItemRegistered(String category, String itemId) {
        TournamentManager tournament = new TournamentManager();
        tournament.distributionKey(itemId);
        if (!this.dataStore.load(tournament)) {
            return false;
        }
        byte[] scheduleId = tournament.index().getBytes();
        try {
            clusterStore.mapLock(scheduleId);
            tournament.dataStore(dataStore);
            launch(tournament);
        } finally {
            clusterStore.mapUnlock(scheduleId);
        }
        return true;
    }

    @Override
    public boolean onItemReleased(String category, String itemId) {
        TournamentManager index = tournamentIndex.remove(itemId);
        if(index==null) return false;
        listeners.forEach(l->l.tournamentClosed(index));
        return false;
    }

    private boolean loadTournamentHeader(String tournamentId){
        TournamentManager tournamentHeader = new TournamentManager();
        tournamentHeader.distributionKey(tournamentId);
        if(!this.dataStore.load(tournamentHeader)) return false;
        byte[] lockKey = tournamentHeader.index().getBytes();
        try{
            clusterStore.mapLock(lockKey);
            tournamentHeader.dataStore(this.dataStore);
            if(tournamentHeader.status() == Tournament.Status.ENDED) return false;
            launch(tournamentHeader);
            return true;
        }finally {
            clusterStore.mapUnlock(lockKey);
        }
        //return true;
    }

    private void createSchedule(TournamentSchedule schedule){
        lookupScheduleKey.addKey(schedule.distributionKey());
        lookupScheduleKey.update();
    }
    private TournamentManager createTournament(TournamentSchedule schedule){
        TournamentManager tournament = new TournamentManager(schedule);
        tournament.dataStore(dataStore);
        dataStore.create(tournament);
        TournamentScheduleStatus status = schedule.status();
        status.index(tournament.distributionKey());
        status.status = Tournament.Status.STARTING;
        dataStore.update(status);
        lookupTournamentKey.addKey(tournament.distributionKey());
        lookupTournamentKey.update();
        lookupScheduleKey.removeKey(schedule.distributionKey());
        lookupScheduleKey.update();
        return tournament;
    }
    private void launch(TournamentManager tournament){
        String tkey = tournament.distributionKey();
        this.tournamentIndex.put(tkey,tournament);
        tournament.setup(this);
        tournament.pendingSchedule = this.serviceContext.schedule(new TournamentCloseMonitor(tournament,this));
        TournamentScheduleStatus status = new TournamentScheduleStatus();
        status.distributionKey(tournament.index());
        status.index(tkey);
        status.status = Tournament.Status.STARTED;
        dataStore.update(status);
    }


    public void closeTournament(String tournamentId){
        TournamentManager index = tournamentIndex.get(tournamentId);
        if(index==null) return;
        index.close();
    }

    public void endTournamentForcefully(String tournamentId){
        logger.warn("Tournament forcefully end ->"+tournamentId);
        TournamentManager tournamentHeaderIndex = tournamentIndex.get(tournamentId);
        if(tournamentHeaderIndex!=null) {
            tournamentHeaderIndex.pendingSchedule.cancel(true);
            endTournament(tournamentHeaderIndex);
        }
    }

    private void endTournament(TournamentManager tournamentHeader){
        serviceContext.schedule(new TournamentEndTask(tournamentHeader));
        TournamentScheduleStatus status = new TournamentScheduleStatus();
        status.distributionKey(tournamentHeader.index());
        status.index(null);
        dataStore.update(status);
        ConfigurableObject schedule = new ConfigurableObject();
        schedule.distributionKey(tournamentHeader.index());
        applicationPreSetup.load(application,schedule);
        schedule.released();
        lookupTournamentKey.removeKey(tournamentHeader.distributionKey());
        lookupTournamentKey.update();
    }

    //distributed operation callbacks
    public Tournament.Instance onTournamentEntered(String tournamentId,String instanceId,String systemId){
        Tournament.Instance _ins = instance(tournamentId,instanceId);
        int joined = _ins.join(systemId);
        logger.warn("Join count->"+joined);
        //if(_ins.join(systemId)==_ins.maxEntries()){
            //closing instance from
            //this.tournamentIndex.get(tournamentId)
        //}
        return _ins;
    }
    public Tournament.Entry onTournamentScored(String instanceId, String systemId, double delta){
        Tournament.Instance _ins = instance(instanceId);
        Tournament.Entry[] score={null};
        _ins.update(systemId,(e)->{
            e.score(delta);
            score[0]=e;
        });
        return score[0];
    }
    public Tournament.RaceBoard onTournamentListed(String instanceId){
        return instance(instanceId).raceBoard();
    }
    public void onTournamentFinished(String instanceId,String systemId){
        logger.warn("finished->"+instanceId+">>"+systemId);
    }
    public void onTournamentSynced(String tournamentId,String instanceId){
        logger.warn("tournament sync->"+tournamentId+">>>"+instanceId);
    }
    public void onTournamentClosed(String tournamentId){
        this.closeTournament(tournamentId);
    }
    public void onTournamentEnded(String tournamentId){
        this.endTournamentForcefully(tournamentId);
    }


}
