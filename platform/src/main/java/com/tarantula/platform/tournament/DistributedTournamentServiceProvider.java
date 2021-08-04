package com.tarantula.platform.tournament;

import com.icodesoftware.*;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.TournamentServiceProvider;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.IndexSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DistributedTournamentServiceProvider implements TournamentServiceProvider {

    private TarantulaLogger logger;
    private ServiceContext serviceContext;
    private DistributionTournamentService distributionTournamentService;
    private final String name;
    private DataStore dataStore;
    private ConcurrentHashMap<String,Tournament.Listener> listeners = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String,TournamentHeader> tournamentIndex = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,TournamentInstanceHeader> instanceIndex = new ConcurrentHashMap<>();
    private IndexSet lookupKey;
    public DistributedTournamentServiceProvider(String gameServiceProviderName){
        this.name = gameServiceProviderName;
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
        byte[] ret = distributionTournamentService.schedule(name(),schedule);
        TournamentHeader tournament = new TournamentHeader();
        Map<String,Object> _map = JsonUtil.toMap(ret);
        tournament.distributionKey(_map.get("tournamentId").toString());
        tournament.fromMap(_map);
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
        byte[] ret = this.distributionTournamentService.join(name(),tournamentId,tid,systemId);
        Tournament.Instance _e = new TournamentInstanceHeader();
        _e.distributionKey(tid);
        _e.fromBinary(ret);
        return _e;
    }

    @Override
    public Tournament.Entry score(String instanceId, String systemId, double delta) {
        byte[] ret = this.distributionTournamentService.score(name(),instanceId,systemId,delta);
        Tournament.Entry _e = new TournamentEntry();
        _e.fromBinary(ret);
        logger.warn("tournament score->"+_e.toJson());
        return _e;
    }
    @Override
    public Tournament.Entry configure(String instanceId, String systemId, byte[] payload) {
        byte[] ret = this.distributionTournamentService.configure(name(),instanceId,systemId,payload);
        Tournament.Entry _e = new TournamentEntry();
        _e.fromBinary(ret);
        logger.warn("tournament configure->"+_e.toJson());
        return _e;
    }
    @Override
    public List<Tournament.Entry> list(String instanceId) {
        return null;
    }
    public String name(){
        return name;
    }
    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        this.lookupKey = new IndexSet("tournament");
        AccessIndex accessIndex = this.serviceContext.accessIndexService().setIfAbsent(name);
        this.lookupKey.distributionKey(accessIndex.distributionKey());
        this.dataStore = serviceContext.dataStore(name.replace("-","_"),serviceContext.partitionNumber());
        this.dataStore.createIfAbsent(this.lookupKey,true);
        this.logger = serviceContext.logger(DistributedTournamentServiceProvider.class);
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
        this.logger.warn("distributed tournament started on lookup key->"+lookupKey.distributionKey());
    }

    @Override
    public void shutdown() throws Exception {
        this.logger.warn("distributed tournament shutdown");
    }
    //distributed operations callbacks
    public Tournament schedule(Tournament.Schedule schedule) {
        TournamentHeader tournament = new TournamentHeader(schedule);
        tournament.dataStore(dataStore);
        dataStore.create(tournament);
        lookupKey.keySet.add(tournament.distributionKey());
        dataStore.update(lookupKey);
        listeners.forEach((k,v)->{
            v.tournamentStarted(tournament);
            v.tournamentClosed(tournament);
            v.tournamentEnded(tournament);
        });
        tournament.setup(instanceIndex);
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
        logger.warn("loading tournament header=>"+tournamentId);
        TournamentHeader tournamentHeader = new TournamentHeader();
        tournamentHeader.distributionKey(tournamentId);
        if(!this.dataStore.load(tournamentHeader)){
            return false;
        }
        tournamentHeader.dataStore(this.dataStore);
        tournamentHeader.setup(instanceIndex);
        tournamentIndex.put(tournamentId,tournamentHeader);
        return true;
    }
}
