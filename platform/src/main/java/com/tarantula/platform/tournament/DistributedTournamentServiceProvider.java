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
        DefaultTournament tournament = new DefaultTournament();
        Map<String,Object> _map = JsonUtil.toMap(ret);
        tournament.distributionKey(_map.get("tournamentId").toString());
        tournament.fromMap(_map);
        return tournament;
    }

    @Override
    public boolean available(String tournamentId) {
        DefaultTournament tournament = new DefaultTournament();
        tournament.distributionKey(tournamentId);
        return dataStore.load(tournament);
    }

    @Override
    public Tournament.Instance join(String s, String s1) {
        return null;
    }

    @Override
    public Tournament.Entry score(String s, String s1, double v) {
        return null;
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

    }

    public Tournament schedule(Tournament.Schedule schedule) {
        DefaultTournament tournament = new DefaultTournament(schedule);
        dataStore.create(tournament);
        listeners.forEach((k,v)->{
            v.tournamentStarted(tournament);
            v.tournamentClosed(tournament);
            v.tournamentEnded(tournament);
        });
        return tournament;
    }
}
