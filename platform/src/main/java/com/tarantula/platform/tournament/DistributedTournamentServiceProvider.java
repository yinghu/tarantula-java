package com.tarantula.platform.tournament;

import com.icodesoftware.*;
import com.icodesoftware.service.ReloadListener;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.TournamentServiceProvider;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.IndexSet;
import com.tarantula.platform.inventory.InventoryServiceProvider;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DistributedTournamentServiceProvider implements TournamentServiceProvider, ReloadListener {

    private static final String CONFIG = "game-tournament-settings";

    private TarantulaLogger logger;
    private ServiceContext serviceContext;
    private DistributionTournamentService distributionTournamentService;
    private final String name;
    private DataStore dataStore;
    private ConcurrentHashMap<String,Tournament.Listener> listeners = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String,TournamentHeader> tournamentIndex = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,TournamentInstanceHeader> instanceIndex = new ConcurrentHashMap<>();
    private IndexSet lookupKey;
    private Configuration configuration;
    private String reloadKey;
    private GameCluster gameCluster;
    private InventoryServiceProvider inventoryServiceProvider;
    public DistributedTournamentServiceProvider(GameCluster gameCluster,InventoryServiceProvider inventoryServiceProvider){
        this.name = (String)gameCluster.property(GameCluster.GAME_SERVICE);
        this.gameCluster = gameCluster;
        this.inventoryServiceProvider = inventoryServiceProvider;
    }

    @Override
    public String registerTournamentListener(Tournament.Listener listener) {
        String key = UUID.randomUUID().toString();
        listeners.put(key,listener);
        tournamentIndex.forEach((k,v)->{
            listeners.forEach((s,l)->{
                l.tournamentStarted(v);
            });
        });
        return key;
    }

    @Override
    public void unregisterTournamentListener(String key) {
        listeners.remove(key);
    }

    @Override
    public boolean register(Tournament.Schedule schedule) {
        return distributionTournamentService.schedule(name(),schedule);
    }

    @Override
    public boolean available(String tournamentId) {
        TournamentHeader tournament = new TournamentHeader();
        tournament.distributionKey(tournamentId);
        return dataStore.load(tournament);
    }

    @Override
    public Tournament.Instance join(String tournamentId, String systemId) {
        String tid = this.distributionTournamentService.register(name(),tournamentId,systemId);
        Tournament.Instance instance = this.distributionTournamentService.join(name(),tournamentId,tid,systemId);
        instance.distributionKey(tid);
        return instance;
    }

    @Override
    public Tournament.Entry score(String instanceId, String systemId, double delta) {
        Tournament.Entry _e = this.distributionTournamentService.score(name(),instanceId,systemId,delta);
        return _e;
    }
    @Override
    public Tournament.Entry configure(String instanceId, String systemId, byte[] payload) {
        Tournament.Entry _e = this.distributionTournamentService.configure(name(),instanceId,systemId,payload);
        return _e;
    }
    public void leave(String instanceId, String systemId){

    }
    @Override
    public Tournament.RaceBoard list(String instanceId) {
        Tournament.RaceBoard ins = this.distributionTournamentService.list(name(),instanceId);
        Collections.sort(ins.list(),new TournamentEntryComparator());
        return ins;
    }
    public String name(){
        return name;
    }
    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        this.configuration = serviceContext.configuration(CONFIG);
        this.lookupKey = new IndexSet(GameCluster.TOURNAMENT_LOOKUP_INDEX);
        this.lookupKey.distributionKey(gameCluster.distributionKey());
        this.dataStore = serviceContext.dataStore(name.replace("-","_"),serviceContext.partitionNumber());
        this.dataStore.createIfAbsent(this.lookupKey,true);
        this.logger = serviceContext.logger(DistributedTournamentServiceProvider.class);
        reloadKey = this.serviceContext.clusterProvider(Distributable.DATA_SCOPE).registerReloadListener(this);
        this.distributionTournamentService = this.serviceContext.clusterProvider(Distributable.DATA_SCOPE).serviceProvider(DistributionTournamentService.NAME);
    }
    @Override
    public void waitForData(){
        ArrayList<String> removed = new ArrayList();
        lookupKey.keySet().forEach((k)->{
            if(!loadTournamentHeader(k)){
                removed.add(k);
            }
        });
        removed.forEach((r)->{
            lookupKey.removeKey(r);
        });
        this.dataStore.update(lookupKey);
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
    //distributed operations callbacks
    public boolean schedule(Tournament.Schedule schedule) {
        boolean scheduled = true;
        if(schedule.schedule().equals(Tournament.ON_DEMAND_SCHEDULE)) {
            TournamentHeader tournament = new TournamentHeader(schedule);
            tournament.dataStore(dataStore);
            dataStore.create(tournament);
            lookupKey.addKey(tournament.distributionKey());
            dataStore.update(lookupKey);
            tournament.setup(instanceIndex,this);
            tournamentIndex.put(tournament.distributionKey(),tournament);
            this.serviceContext.schedule(new TournamentStartMonitor(tournament,this));
        }
        else if(schedule.schedule().equals(Tournament.DAILY_SCHEDULE)){
            
        }
        else if(schedule.schedule().equals(Tournament.DAILY_SCHEDULE)){

        }
        else if(schedule.schedule().equals(Tournament.MONTHLY_SCHEDULE)){

        }
        else if(schedule.schedule().equals(Tournament.MONTHLY_SCHEDULE)){

        }
        else{
            this.logger.warn("Schedule->"+schedule.schedule()+" not supported");
            scheduled = false;
        }
        return scheduled;
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
    public List<Tournament.History> history(String systemId){
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

    @Override
    public void reload() {
        logger.warn("reloading tournament");
    }
    public void atMidnight(){
        serviceContext.schedule(new TournamentMidnightTask(this));
    }
    void midnightCheck(){
        //midnight close/launch daily/weekly/monthly tournaments
    }
    void onTournamentStart(TournamentHeader tournamentHeader){
        listeners.forEach((k,l)->l.tournamentStarted(tournamentHeader));
    }
    void onTournamentClose(TournamentHeader tournamentHeader){
        listeners.forEach((k,l)->l.tournamentClosed(tournamentHeader));
        this.serviceContext.schedule(new TournamentEndMonitor(tournamentHeader,this));
    }
    void onTournamentEnd(TournamentHeader tournamentHeader){
        listeners.forEach((k,l)->l.tournamentEnded(tournamentHeader));
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
}
