package com.tarantula.platform.tournament;

import com.icodesoftware.*;
import com.icodesoftware.service.ConfigurationServiceProvider;
import com.icodesoftware.service.ReloadListener;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.TournamentServiceProvider;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.IndexSet;
import com.tarantula.platform.inventory.InventoryServiceProvider;
import com.tarantula.platform.item.DistributionItemService;
import com.tarantula.platform.service.ApplicationPreSetup;
import com.tarantula.platform.service.ClusterConfigurationCallback;
import com.tarantula.platform.util.SystemUtil;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class PlatformTournamentServiceProvider implements TournamentServiceProvider, ReloadListener, ConfigurationServiceProvider, ClusterConfigurationCallback {

    private static final String CONFIG = "game-tournament-settings";
    private static final String DS_SUFFIX = "_tournament";

    private TarantulaLogger logger;
    private ServiceContext serviceContext;
    private DistributionTournamentService distributionTournamentService;
    private DistributionItemService distributionItemService;
    private final String name;
    private DataStore dataStore;
    private CopyOnWriteArrayList<Tournament.Listener> listeners = new CopyOnWriteArrayList<>();

    private ConcurrentHashMap<String,TournamentHeader> tournamentIndex = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,TournamentInstanceHeader> instanceIndex = new ConcurrentHashMap<>();

    private IndexSet lookupTournamentKey;
    private IndexSet lookupScheduleKey;
    private Configuration configuration;
    private String reloadKey;
    private GameCluster gameCluster;
    private ApplicationPreSetup applicationPreSetup;
    private Descriptor application;
    private InventoryServiceProvider inventoryServiceProvider;
    public PlatformTournamentServiceProvider(GameCluster gameCluster, InventoryServiceProvider inventoryServiceProvider){
        this.name = (String)gameCluster.property(GameCluster.GAME_SERVICE);
        this.gameCluster = gameCluster;
        this.inventoryServiceProvider = inventoryServiceProvider;
    }

    @Override
    public void registerTournamentListener(Tournament.Listener listener) {
        listeners.add(listener);
    }
    @Override
    public String registerConfigurableListener(Descriptor descriptor, Configurable.Listener listener) {
        application = descriptor;
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
        String tid = this.distributionTournamentService.register(name,tournamentId,systemId);
        Tournament.Instance instance = this.distributionTournamentService.join(name,tournamentId,tid,systemId);
        instance.distributionKey(tid);
        return instance;
    }

    @Override
    public Tournament.Entry score(String instanceId, String systemId, double delta) {
        Tournament.Entry _e = this.distributionTournamentService.score(name,instanceId,systemId,delta);
        return _e;
    }
    @Override
    public Tournament.Entry configure(String instanceId, String systemId, byte[] payload) {
        Tournament.Entry _e = this.distributionTournamentService.configure(name,instanceId,systemId,payload);
        return _e;
    }
    public void leave(String instanceId, String systemId){

    }
    @Override
    public Tournament.RaceBoard list(String instanceId) {
        Tournament.RaceBoard ins = this.distributionTournamentService.list(name,instanceId);
        Collections.sort(ins.list(),new TournamentEntryComparator());
        return ins;
    }
    public List<Tournament> list(){
        ArrayList<Tournament> _tms = new ArrayList<>();
        tournamentIndex.forEach((k,v)->_tms.add(v));
        return _tms;
    }
    public String name(){
        return "TournamentService";
    }
    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        this.applicationPreSetup = SystemUtil.applicationPreSetup((String)gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
        this.configuration = serviceContext.configuration(CONFIG);
        this.lookupTournamentKey = new IndexSet(GameCluster.TOURNAMENT_LOOKUP_INDEX);
        this.lookupTournamentKey.distributionKey(gameCluster.distributionKey());
        this.lookupScheduleKey = new IndexSet(GameCluster.TOURNAMENT_SCHEDULE_LOOKUP_INDEX);
        this.lookupScheduleKey.distributionKey(gameCluster.distributionKey());
        this.dataStore = serviceContext.dataStore(name.replace("-","_")+DS_SUFFIX,serviceContext.partitionNumber());
        this.dataStore.createIfAbsent(this.lookupTournamentKey,true);
        this.dataStore.createIfAbsent(this.lookupScheduleKey,true);
        this.lookupTournamentKey.dataStore(this.dataStore);
        this.lookupScheduleKey.dataStore(this.dataStore);
        this.logger = this.serviceContext.logger(PlatformTournamentServiceProvider.class);
        this.reloadKey = this.serviceContext.clusterProvider(Distributable.INTEGRATION_SCOPE).registerReloadListener(this);
        this.distributionTournamentService = this.serviceContext.clusterProvider(Distributable.DATA_SCOPE).serviceProvider(DistributionTournamentService.NAME);
        this.distributionItemService = this.serviceContext.clusterProvider(Distributable.DATA_SCOPE).serviceProvider(DistributionItemService.NAME);
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
        this.logger.warn("distributed tournament started pending pool size->"+configuration.property("pendingTournamentPoolSize"));
    }

    @Override
    public void shutdown() throws Exception {
        this.logger.warn("distributed tournament shutdown");
        this.serviceContext.clusterProvider(Distributable.DATA_SCOPE).unregisterReloadListener(reloadKey);
    }

    public Tournament tournament(String tournamentId){//schedule node
        TournamentHeader tournament = tournamentIndex.get(tournamentId);
        return tournament;
    }
    public Tournament.Instance instance(String tournamentId,String instanceId){//instance node
        TournamentHeader tournament = this.tournamentIndex.get(tournamentId);
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
    private boolean loadTournamentHeader(String tournamentId){
        TournamentHeader tournamentHeader = new TournamentHeader();
        tournamentHeader.distributionKey(tournamentId);
        if(!this.dataStore.load(tournamentHeader)) return false;
        if(TimeUtil.expired(tournamentHeader.closeTime())){
            logger.warn("Tournament is expired and set to end");
            return false;
        }
        tournamentHeader.dataStore(this.dataStore);
        tournamentIndex.put(tournamentId,tournamentHeader);
        this.serviceContext.schedule(new TournamentCloseMonitor(tournamentHeader,this));
        if(distributionTournamentService.localManaged(tournamentHeader.distributionKey())) tournamentHeader.setup(instanceIndex,this);
        return true;
    }
    private void createSchedule(TournamentSchedule schedule){
        this.dataStore.create(schedule);
        lookupScheduleKey.addKey(schedule.distributionKey());
        lookupScheduleKey.update();
    }
    @Override
    public void reload() {
        logger.warn("reloading tournament");
    }
    public void atMidnight(){
        serviceContext.schedule(new TournamentMidnightTask(this));
    }
    void midnightCheck(){
        //midnight close/launch daily/weekly/monthly tournaments
        this.lookupScheduleKey.keySet().forEach(k->{
                TournamentSchedule schedule = new TournamentSchedule();
                schedule.distributionKey(k);
                if(dataStore.load(schedule)&&schedule.startTime().getDayOfYear() == LocalDateTime.now().getDayOfYear()){
                    if(distributionTournamentService.trySchedule(name,k)){
                        Tournament tournament = createTournament(schedule);
                        this.distributionItemService.register(name,name(),"tournament",tournament.distributionKey());
                        distributionTournamentService.scheduleFinished(name,k);
                    }
                }

        });
    }
    void onTournamentStart(TournamentHeader tournamentHeader){

    }
    void onTournamentClose(TournamentHeader tournamentHeader){
        this.serviceContext.schedule(new TournamentEndMonitor(tournamentHeader,this));
    }
    void onTournamentEnd(TournamentHeader tournamentHeader){

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
        TournamentScheduleParser parser = new TournamentScheduleParser();
        parser.distributionKey(scheduleId);
        if(this.applicationPreSetup.load(serviceContext,application,parser)) return parser.prize();
        return new HashMap<>();
    }

    @Override
    public <T extends Configurable> void register(T t) {
        TournamentScheduleParser parser = (TournamentScheduleParser)t;
        TournamentSchedule schedule = parser.schedule();
        if(schedule.schedule().equals(Tournament.ON_DEMAND_SCHEDULE)){
            Tournament tournament = createTournament(schedule);
            distributionItemService.register(name,name(),t.configurationCategory(),tournament.distributionKey());
        }
        else if(schedule.schedule().equals(Tournament.DAILY_SCHEDULE)){
            this.createSchedule(schedule);
        }
        else if(schedule.schedule().equals(Tournament.WEEKLY_SCHEDULE)){
            this.createSchedule(schedule);
        }
        else if(schedule.schedule().equals(Tournament.MONTHLY_SCHEDULE)){
            this.createSchedule(schedule);
        }
        else{
            throw new UnsupportedOperationException(schedule.schedule());
        }
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
        return false;
    }
    private Tournament createTournament(TournamentSchedule schedule){
        TournamentHeader tournament = new TournamentHeader(schedule);
        tournament.dataStore(dataStore);
        dataStore.create(tournament);
        lookupTournamentKey.addKey(tournament.distributionKey());
        lookupTournamentKey.update();
        lookupScheduleKey.removeKey(schedule.distributionKey());
        lookupScheduleKey.update();
        return tournament;
    }
    private void launch(TournamentHeader tournament){
        this.tournamentIndex.put(tournament.distributionKey(),tournament);
        this.serviceContext.schedule(new TournamentStartMonitor(tournament,this));
        if(this.distributionTournamentService.localManaged(tournament.distributionKey())) tournament.setup(instanceIndex,this);
    }
    public boolean trySchedule(String scheduleId){
        logger.warn("tournament schedule ready to launch ->"+scheduleId);
        return true;
    }
    public boolean finishSchedule(String scheduleId){
        logger.warn("tournament schedule launched ->"+scheduleId);
        return true;
    }
}
