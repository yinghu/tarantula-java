package com.tarantula.platform.tournament;

import com.icodesoftware.*;
import com.icodesoftware.service.ReloadListener;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.TournamentServiceProvider;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.IndexSet;
import com.tarantula.platform.inventory.ApplicationRedeemer;
import com.tarantula.platform.inventory.InventoryServiceProvider;
import com.tarantula.platform.item.Application;

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
    public Tournament register(Tournament.Schedule schedule) {
        Tournament tournament = distributionTournamentService.schedule(name(),schedule);
        return tournament;
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
        ArrayList removed = new ArrayList();
        lookupKey.keySet.forEach((k)->{
            if(!loadTournamentHeader(k)){
                removed.add(k);
            }
        });
        removed.forEach((r)->{
            lookupKey.keySet.remove(r);
        });
        this.dataStore.update(lookupKey);
    }
    @Override
    public void start() throws Exception {
        this.logger.warn("distributed tournament started pending pool size->"+configuration.property("pendingTournamentPoolSize"));
    }

    @Override
    public void shutdown() throws Exception {
        this.logger.warn("distributed tournament shutdown");
        this.serviceContext.clusterProvider(Distributable.DATA_SCOPE).unregisterReloadListener(reloadKey);
    }
    //distributed operations callbacks
    public Tournament schedule(Tournament.Schedule schedule) {
        logger.warn("Schedule key->"+schedule.distributionKey());
        TournamentHeader tournament = new TournamentHeader(schedule);
        tournament.dataStore(dataStore);
        dataStore.create(tournament);
        lookupKey.keySet.add(tournament.distributionKey());
        dataStore.update(lookupKey);
        listeners.forEach((k,v)-> v.tournamentStarted(tournament));
        tournament.setup(instanceIndex,this);
        tournamentIndex.put(tournament.distributionKey(),tournament);

        return tournament;
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
    private boolean loadTournamentHeader(String tournamentId){
        TournamentHeader tournamentHeader = new TournamentHeader();
        tournamentHeader.distributionKey(tournamentId);
        if(!this.dataStore.load(tournamentHeader)) return false;
        if(TimeUtil.expired(tournamentHeader.endTime)){
            logger.warn("Tournament is expired and set to end");
            return false;
        }
        tournamentHeader.dataStore(this.dataStore);
        tournamentIndex.put(tournamentId,tournamentHeader);
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
        //midnight close/launch daily tournaments
    }
    void monitorInstanceOnClose(TournamentHeader tournamentHeader,TournamentInstanceHeader instanceHeader){
        this.serviceContext.schedule(new TournamentInstanceCloseMonitor(tournamentHeader,instanceHeader));
        logger.warn(">>on-close->"+instanceHeader.distributionKey());
    }
    void monitorInstanceOnEnd(TournamentHeader tournamentHeader,TournamentInstanceHeader instanceHeader){
        this.serviceContext.schedule(new TournamentInstanceEndMonitor(tournamentHeader,instanceHeader));
        logger.warn(">>on-end->"+instanceHeader.distributionKey());
    }
    void monitorRegistry(TournamentHeader tournamentHeader,TournamentRegistry tournamentRegistry){
        this.serviceContext.schedule(new TournamentRegistryCloseMonitor(tournamentHeader,tournamentRegistry));
        logger.warn(">>on-register->"+tournamentRegistry.distributionKey());
    }
    void onPrize(String systemId,TournamentPrize prize){
        logger.warn("Prize->"+systemId+"<>"+prize.setup().toJson().toString());
        boolean redeemed = inventoryServiceProvider.redeem(systemId,prize);
        logger.warn("prize redeemed->"+redeemed);
    }
}
