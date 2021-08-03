package com.tarantula.platform.tournament;

import com.icodesoftware.DataStore;
import com.icodesoftware.Distributable;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.Tournament;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.TournamentServiceProvider;
import com.icodesoftware.util.JsonUtil;

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
    public DistributedTournamentServiceProvider(String gameServiceProviderName){
        this.name = gameServiceProviderName;
    }

    @Override
    public String registerTournamentListener(Tournament.Listener listener) {
        String key = UUID.randomUUID().toString();
        listeners.put(key,listener);
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
        String tid = this.distributionTournamentService.join(name(),tournamentId,systemId);
        byte[] ret = this.distributionTournamentService.enter(name(),tournamentId,tid,systemId);
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
        logger.warn("tournament score->"+_e.score(0));
        return _e;
    }

    @Override
    public List<Tournament.Entry> tournamentEntries(String s) {
        return null;
    }
    public String name(){
        return name;
    }
    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        this.dataStore = serviceContext.dataStore(name.replace("-","_"),serviceContext.partitionNumber());
        this.logger = serviceContext.logger(DistributedTournamentServiceProvider.class);
        this.distributionTournamentService = this.serviceContext.clusterProvider(Distributable.DATA_SCOPE).serviceProvider(DistributionTournamentService.NAME);
        this.logger.warn("distributed tournament setup");
    }

    @Override
    public void start() throws Exception {
        this.logger.warn("distributed tournament started");
    }

    @Override
    public void shutdown() throws Exception {
        this.logger.warn("distributed tournament shutdown");
    }
    //distributed operations callbacks
    public Tournament schedule(Tournament.Schedule schedule) {
        TournamentHeader tournament = new TournamentHeader(schedule);
        dataStore.create(tournament);
        listeners.forEach((k,v)->{
            v.tournamentStarted(tournament);
            v.tournamentClosed(tournament);
            v.tournamentEnded(tournament);
        });
        return tournament;
    }
    public Tournament tournament(String tournamentId){//schedule node
        JoinTournament tournament = new JoinTournament();
        tournament.distributionKey(tournamentId);
        dataStore.load(tournament);
        tournament.dataStore(dataStore);
        return tournament;
    }
    public Tournament.Instance instance(String tournamentId,String instanceId){//instance node
        PlayTournament tournament = new PlayTournament();
        tournament.distributionKey(tournamentId);
        dataStore.load(tournament);
        tournament.dataStore(dataStore);
        return tournament.findInstance(instanceId);
    }
    public Tournament.Instance instance(String instanceId){
        TournamentInstanceHeader tournament = new TournamentInstanceHeader();
        tournament.distributionKey(instanceId);
        dataStore.load(tournament);
        tournament.dataStore(dataStore);
        return tournament;
    }//instance node
}
