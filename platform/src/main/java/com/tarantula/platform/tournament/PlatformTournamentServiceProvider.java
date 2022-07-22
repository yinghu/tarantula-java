package com.tarantula.platform.tournament;

import com.icodesoftware.*;
import com.icodesoftware.service.ConfigurationServiceProvider;
import com.icodesoftware.service.ReloadListener;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.TournamentServiceProvider;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.IndexSet;
import com.tarantula.platform.inventory.PlatformInventoryServiceProvider;
import com.tarantula.platform.item.ConfigurableObject;
import com.tarantula.platform.item.DistributionItemService;
import com.tarantula.platform.service.ApplicationPreSetup;
import com.tarantula.platform.service.ClusterConfigurationCallback;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class PlatformTournamentServiceProvider implements TournamentServiceProvider, ReloadListener, ConfigurationServiceProvider, ClusterConfigurationCallback {

    private static final String CONFIG = "game-tournament-settings";

    private TarantulaLogger logger;
    private ServiceContext serviceContext;
    private DistributionTournamentService distributionTournamentService;
    private DistributionItemService distributionItemService;
    private final String gameServiceName;
    private DataStore dataStore;
    private CopyOnWriteArrayList<Tournament.Listener> listeners = new CopyOnWriteArrayList<>();

    private ConcurrentHashMap<String,TournamentHeaderIndex> tournamentIndex = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,TournamentInstanceHeader> instanceIndex = new ConcurrentHashMap<>();

    private IndexSet lookupTournamentKey;
    private IndexSet lookupScheduleKey;

    private int pendingTournamentPoolSize =  100;
    private int minDurationHoursPerSchedule = 1;
    private int minDurationMinutesPerInstance =  5;
    private String reloadKey;
    private GameCluster gameCluster;
    private ApplicationPreSetup applicationPreSetup;
    private Descriptor application;
    private PlatformInventoryServiceProvider inventoryServiceProvider;

    public PlatformTournamentServiceProvider(GameCluster gameCluster, PlatformInventoryServiceProvider inventoryServiceProvider){
        this.gameServiceName = (String)gameCluster.property(GameCluster.GAME_SERVICE);
        this.gameCluster = gameCluster;
        this.inventoryServiceProvider = inventoryServiceProvider;
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
        TournamentHeader tournament = new TournamentHeader();
        tournament.distributionKey(tournamentId);
        return dataStore.load(tournament);
    }

    @Override
    public Tournament.Instance join(String tournamentId, String systemId) {
        String tid = this.distributionTournamentService.register(gameServiceName,tournamentId,systemId);
        Tournament.Instance instance = this.distributionTournamentService.join(gameServiceName,tournamentId,tid,systemId);
        instance.distributionKey(tid);
        return instance;
    }

    @Override
    public Tournament.Entry score(String instanceId, String systemId, double delta) {
        Tournament.Entry _e = this.distributionTournamentService.score(gameServiceName,instanceId,systemId,delta);
        return _e;
    }
    @Override
    public Tournament.Entry configure(String instanceId, String systemId, byte[] payload) {
        Tournament.Entry _e = this.distributionTournamentService.configure(gameServiceName,instanceId,systemId,payload);
        return _e;
    }
    public void leave(String instanceId, String systemId){

    }
    @Override
    public Tournament.RaceBoard list(String instanceId) {
        Tournament.RaceBoard ins = this.distributionTournamentService.list(gameServiceName,instanceId);
        Collections.sort(ins.list(),new TournamentEntryComparator());
        return ins;
    }
    public List<Tournament> list(){
        ArrayList<Tournament> _tms = new ArrayList<>();
        tournamentIndex.forEach((k,v)->
        {
            if(v.tournamentHeader.status == Tournament.Status.STARTED) _tms.add(v.tournamentHeader);
        });
        return _tms;
    }
    public String name(){
        return "tournament";
    }
    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        this.applicationPreSetup = gameCluster.applicationPreSetup();//SystemUtil.applicationPreSetup((String)gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
        Configuration configuration = serviceContext.configuration(CONFIG);
        this.pendingTournamentPoolSize = ((Number)configuration.property("pendingTournamentPoolSize")).intValue();
        this.minDurationHoursPerSchedule = ((Number)configuration.property("minDurationHoursPerSchedule")).intValue();
        this.minDurationMinutesPerInstance = ((Number)configuration.property("minDurationMinutesPerInstance")).intValue();
        this.lookupTournamentKey = new IndexSet(GameCluster.TOURNAMENT_LOOKUP_INDEX);
        this.lookupTournamentKey.distributionKey(gameCluster.distributionKey());
        this.lookupScheduleKey = new IndexSet(GameCluster.TOURNAMENT_SCHEDULE_LOOKUP_INDEX);
        this.lookupScheduleKey.distributionKey(gameCluster.distributionKey());
        this.dataStore = applicationPreSetup.dataStore(gameCluster,name());//serviceContext.dataStore(name.replace("-","_")+DS_SUFFIX,serviceContext.partitionNumber());
        this.dataStore.createIfAbsent(this.lookupTournamentKey,true);
        this.dataStore.createIfAbsent(this.lookupScheduleKey,true);
        this.lookupTournamentKey.dataStore(this.dataStore);
        this.lookupScheduleKey.dataStore(this.dataStore);
        this.logger = this.serviceContext.logger(PlatformTournamentServiceProvider.class);
        this.reloadKey = this.serviceContext.clusterProvider().registerReloadListener(this);
        this.distributionTournamentService = this.serviceContext.clusterProvider().serviceProvider(DistributionTournamentService.NAME);
        this.distributionItemService = this.serviceContext.clusterProvider().serviceProvider(DistributionItemService.NAME);
    }
    @Override
    public void waitForData(){
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
    }
    @Override
    public void start() throws Exception {
        this.serviceContext.schedule(new TournamentMidnightTask(this));
        this.logger.warn("Tournament service provider started with pending pool size->["+pendingTournamentPoolSize+"] on ->"+gameServiceName);
    }

    @Override
    public void shutdown() throws Exception {
        this.logger.warn("distributed tournament shutdown");
        this.serviceContext.clusterProvider().unregisterReloadListener(reloadKey);
    }

    public Tournament tournament(String tournamentId){//schedule node
        TournamentHeader tournament = tournamentIndex.get(tournamentId).tournamentHeader;
        return tournament;
    }
    public Tournament.Instance instance(String tournamentId,String instanceId){//instance node
        TournamentHeader tournament = this.tournamentIndex.get(tournamentId).tournamentHeader;
        return tournament.lookup(instanceId);
    }
    public Tournament.Instance instance(String instanceId){//instance node
        TournamentInstanceHeader tournament = this.instanceIndex.get(instanceId);
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
        tournamentIndex.forEach((k,v)->{
            if(v.partitionId == partition){
                if(v.localManaged && !localMember){
                    logger.warn("release tournament->"+v);
                    v.localManaged = false;
                }
                else if(!v.localManaged && localMember){
                    logger.warn("take over tournament->"+v);
                    v.localManaged = true;
                    v.tournamentHeader.setup(this.instanceIndex,this);
                }
            }
        });
    }
    public void atMidnight(){
        serviceContext.schedule(new TournamentMidnightTask(this));
    }
    void midnightCheck(){
        //midnight close/launch daily/weekly/monthly tournaments
        this.lookupScheduleKey.keySet().forEach(k->{
            TournamentSchedule schedule = new TournamentSchedule();
            schedule.distributionKey(k);
            if(applicationPreSetup.load(application,schedule)){
                if(schedule.startTime().getDayOfYear() == LocalDateTime.now().getDayOfYear()){
                    if(distributionTournamentService.localManaged(k).localManaged){
                        Tournament tournament = createTournament(schedule);
                        this.distributionItemService.register(gameServiceName,name(),schedule.configurationCategory(),tournament.distributionKey());
                    }
                }
            }
        });
    }
    void onTournamentStart(Tournament tournamentHeader){
        this.distributionItemService.register(gameServiceName,name(),"TournamentSchedule",tournamentHeader.distributionKey());
    }
    void onTournamentClose(TournamentHeader tournamentHeader){
        this.distributionTournamentService.closeTournament(gameServiceName,tournamentHeader.distributionKey());
        this.serviceContext.schedule(new TournamentEndMonitor(tournamentHeader,this));
    }
    void onTournamentEnd(TournamentHeader tournamentHeader){
        endTournament(tournamentHeader);
        this.distributionItemService.release(gameServiceName,name(),"TournamentSchedule",tournamentHeader.distributionKey());
    }
    void monitorInstanceOnClose(TournamentHeader tournamentHeader,TournamentInstanceHeader instanceHeader){
        this.serviceContext.schedule(new TournamentInstanceCloseMonitor(tournamentHeader,instanceHeader));
    }
    void monitorInstanceOnEnd(TournamentHeader tournamentHeader,TournamentInstanceHeader instanceHeader){
        this.serviceContext.schedule(new TournamentInstanceEndMonitor(tournamentHeader,instanceHeader));
    }
    void monitorRegistry(TournamentHeader tournamentHeader,TournamentRegistry tournamentRegistry){
        this.serviceContext.schedule(new TournamentRegistryCloseMonitor(tournamentHeader,tournamentRegistry));
    }
    void onPrize(String systemId,TournamentPrize prize){
        inventoryServiceProvider.redeem(systemId,prize);
    }
    void log(String message){
        logger.warn(message);
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
        if(!t.configurationCategory().equals("TournamentSchedule")) throw new RuntimeException(t.configurationCategory()+" cannot be registered");
        TournamentScheduleStatus status = new TournamentScheduleStatus();
        status.distributionKey(t.distributionKey());
        dataStore.createIfAbsent(status,true);
        if(status.index() != null) throw new RuntimeException("schedule is running on tournament ["+status.index()+"]");
        TournamentSchedule schedule = new TournamentSchedule((ConfigurableObject) t);
        if(schedule.durationHoursPerSchedule()<minDurationHoursPerSchedule) throw new RuntimeException("min hours per schedule less than ["+minDurationHoursPerSchedule+"]");
        if(schedule.durationMinutesPerInstance()<minDurationMinutesPerInstance) throw new RuntimeException("min minutes per instance less than ["+minDurationMinutesPerInstance+"]");
        if(TimeUtil.expired(schedule.startTime())) throw new RuntimeException("start time already expired");
        t.registered();
        switch (schedule.schedule()){
            case Tournament.DAILY_SCHEDULE:
            case Tournament.WEEKLY_SCHEDULE:
            case Tournament.MONTHLY_SCHEDULE:
                LocalDateTime _current = LocalDateTime.now();
                if(schedule.startTime().getYear() ==_current.getYear() && schedule.startTime().getDayOfYear() ==_current.getDayOfYear()) throw new RuntimeException("start time already expired on daily midnight launch");
                createSchedule(schedule);
                break;
            case Tournament.ON_DEMAND_SCHEDULE:
                Tournament tournament = createTournament(schedule);
                serviceContext.schedule(new TournamentStartMonitor(tournament,this));
                break;
            default:
                throw new RuntimeException("schedule plan not supported ["+schedule.schedule()+"]");
        }
    }
    @Override
    public <T extends Configurable> void release(T t) {
        TournamentScheduleStatus status = new TournamentScheduleStatus();
        status.distributionKey(t.distributionKey());
        dataStore.createIfAbsent(status,true);
        if(status.index() == null) {
            lookupScheduleKey.removeKey(t.distributionKey());
            lookupScheduleKey.update();
            t.released();
            distributionItemService.release(gameServiceName, name(), t.configurationTypeId(), t.distributionKey());
            return;
        }
        distributionTournamentService.endTournament(gameServiceName,status.index());
        distributionItemService.release(gameServiceName,name(),t.configurationTypeId(),t.distributionKey());
    }

    @Override
    public boolean onRegister(String category, String itemId) {
        TournamentHeader tournament = new TournamentHeader();
        tournament.distributionKey(itemId);
        if(!this.dataStore.load(tournament)){
            return false;
        }
        tournament.dataStore(dataStore);
        launch(tournament);
        return true;
    }

    @Override
    public boolean onRelease(String category, String itemId) {
        tournamentIndex.remove(itemId);
        return false;
    }

    private boolean loadTournamentHeader(String tournamentId){
        TournamentHeader tournamentHeader = new TournamentHeader();
        tournamentHeader.distributionKey(tournamentId);
        if(!this.dataStore.load(tournamentHeader)) return false;
        tournamentHeader.dataStore(this.dataStore);
        if(tournamentHeader.status == Tournament.Status.ENDED){
            TournamentScheduleStatus status = new TournamentScheduleStatus();
            status.distributionKey(tournamentHeader.index());
            status.index(null);
            dataStore.update(status);
            ConfigurableObject configurableObject = new ConfigurableObject();
            configurableObject.distributionKey(tournamentHeader.index());
            applicationPreSetup.load(application,configurableObject);
            configurableObject.released();
            return false;
        }
        launch(tournamentHeader);
        return true;
    }

    private void createSchedule(TournamentSchedule schedule){
        lookupScheduleKey.addKey(schedule.distributionKey());
        lookupScheduleKey.update();
    }
    private Tournament createTournament(TournamentSchedule schedule){
        TournamentHeader tournament = new TournamentHeader(schedule);
        tournament.dataStore(dataStore);
        dataStore.create(tournament);
        TournamentScheduleStatus status = schedule.status();
        status.index(tournament.distributionKey());
        dataStore.update(status);
        lookupTournamentKey.addKey(tournament.distributionKey());
        lookupTournamentKey.update();
        lookupScheduleKey.removeKey(schedule.distributionKey());
        lookupScheduleKey.update();
        return tournament;
    }
    private void launch(TournamentHeader tournament){
        String tkey = tournament.distributionKey();
        TournamentHeaderIndex index = this.distributionTournamentService.localManaged(tkey);
        index.tournamentHeader = tournament;
        this.tournamentIndex.put(tkey,index);
        if(!index.localManaged) return;
        tournament.setup(instanceIndex,this);
        this.serviceContext.schedule(new TournamentCloseMonitor(tournament,this));
    }

    public void endTournamentForcefully(String tournamentId){
        logger.warn("Tournament forcefully end ->"+tournamentId);
        TournamentHeaderIndex tournamentHeaderIndex = tournamentIndex.remove(tournamentId);
        if(tournamentHeaderIndex!=null) onTournamentEnd(tournamentHeaderIndex.tournamentHeader);
    }

    private void endTournament(TournamentHeader tournamentHeader){
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

}
