package com.tarantula.platform.presence.pvp;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.IntegerRangeKey;
import com.icodesoftware.util.ScheduleRunner;
import com.icodesoftware.util.SnowflakeKey;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.game.SimpleStub;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.configuration.SeasonCredentialConfiguration;
import com.tarantula.platform.event.PortableEventRegistry;
import com.tarantula.platform.item.ConfigurableObject;
import com.tarantula.platform.item.ConfigurableObjectQuery;
import com.tarantula.platform.item.PlatformItemServiceProvider;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class PlatformPVPBattleServiceProvider extends PlatformItemServiceProvider implements Configurable.Listener<SeasonCredentialConfiguration>{

    private static final long CURRENT_SEASON_INDEX = 0;
    public static final String NAME = "pvp_battle";
    private int teamCreationWaitingTime = 5;
    private int seasonTimeGap = 10; //minutes
    private int seasonRunningDays = 12;//days
    private int reMatchWaitingTimeMinutes = 60; //minutes
    private ConcurrentHashMap<Long, SeasonCredentialConfiguration.Season> seasons = new ConcurrentHashMap();
    private ConcurrentHashMap<Integer,League> leagues = new ConcurrentHashMap<>();
    private ClusterProvider.ClusterStore scheduleStore;

    private final SeasonRuntime rotation = new SeasonRuntime();

    private DataStore battleHistory;
    private DataStore cooldownStore;
    private DataStore matchMakingStore;
    private String gameEndTopic = GameEndEvent.GAME_END_TOPIC;
    private JsonObject battleLogMockData;

    public PlatformPVPBattleServiceProvider(PlatformGameServiceProvider gameServiceProvider){
        super(gameServiceProvider,NAME);
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        super.setup(serviceContext);
        Configuration configuration = serviceContext.configuration("game-presence-settings");
        Configuration mockDataConfig = serviceContext.configuration("pvp-mock-data");
        JsonObject pvp = ((JsonElement)configuration.property("pvp")).getAsJsonObject();
        battleLogMockData = ((JsonElement)mockDataConfig.property("battleLogMockData")).getAsJsonObject();
        teamCreationWaitingTime = pvp.get("waitingMinutesPerTeamFormation").getAsInt();
        seasonTimeGap = pvp.get("seasonTimeGapMinutes").getAsInt();
        seasonRunningDays = pvp.get("seasonRunningDays").getAsInt();
        reMatchWaitingTimeMinutes = pvp.get("reMatchWaitingTimeMinutes").getAsInt();
        this.dataStore = applicationPreSetup.dataStore(gameCluster,NAME);
        this.cooldownStore = applicationPreSetup.localDataStore(gameCluster,NAME+"_cooldown");
        this.matchMakingStore = applicationPreSetup.localDataStore(gameCluster,NAME+"_match_making");
        this.battleHistory = applicationPreSetup.dataStore(gameCluster,NAME+"_history");
        this.scheduleStore = this.serviceContext.clusterProvider().clusterStore(ClusterProvider.ClusterStore.SMALL,gameCluster.typeId()+"."+NAME);
        this.logger = JDKLogger.getLogger(PlatformPVPBattleServiceProvider.class);
        this.logger.warn("PVP battle service provider started on ->"+gameServiceName);
        this.serviceContext.eventService().registerEventListener(gameEndTopic,e->{
            onEvent(e);
            return true;
        });
        this.platformGameServiceProvider.configurationServiceProvider().addConfigurableListener(OnAccess.SEASON,this);
    }


    public MatchMaking matchMaking(Session session){
        TeamFormationIndex teamFormationIndex = teamFormationIndex(session.distributionId());
        if(teamFormationIndex.teamId==0){
            return MatchMaking.failure(PvpErrorCode.NO_TEAM_FORMATION,"no defense team created");
        }
        List<BattleTeam> matches = new ArrayList<>();
        findMatches(session).forEach(rating -> {
            BattleTeam defenseTeam = defenseTeam(rating);
            if(defenseTeam != null){
                //calculate the estimated pvp points if the player wins.
                defenseTeam.battled = true; //sampling
                defenseTeam.battlePoint = 100; //sampling
                defenseTeam.winPointsEstimated = 100;//sampling
                defenseTeam.elo = rating.level();
                matches.add(defenseTeam);
            }
        });
        MatchMaking matchMaking = MatchMaking.success(teamFormationIndex.timestamp(),matches);
        return matchMaking;
    }

    public TeamFormationResponse saveDefenseTeam(Session session,byte[] content){
        TeamFormationIndex teamFormationIndex = new TeamFormationIndex();
        teamFormationIndex.distributionId(session.distributionId());
        dataStore.createIfAbsent(teamFormationIndex,true);
        if(!teamFormationIndex.expired()) return TeamFormationResponse.failureOnDefenseTeam(teamFormationIndex.timestamp());
        BattleTeam defenseTeam = BattleTeam.parse(content);
        defenseTeam.playerId = session.distributionId();
        defenseTeam.save(dataStore,teamFormationIndex,teamCreationWaitingTime);
        Rating rating = platformGameServiceProvider.presenceServiceProvider().rating(session);
        this.serviceContext.eventService().publish(new TeamFormationEvent(session.distributionId(),rating.level()));
        return TeamFormationResponse.responseOnDefenseTeam(teamFormationIndex.timestamp());
    }

    public TeamFormationResponse saveOffenseTeam(Session session,byte[] content){
        BattleTeam defenseTeam = BattleTeam.parse(content);
        defenseTeam.playerId = session.distributionId();
        if(!dataStore.create(defenseTeam)){
            return TeamFormationResponse.retryOffenseTeam();
        }
        return TeamFormationResponse.responseOnOffenseTeam(defenseTeam.distributionId());
    }

    public BattleTeam defenseTeam(Rating rating){
        TeamFormationIndex teamFormationIndex = new TeamFormationIndex();
        teamFormationIndex.distributionId(rating.distributionId());
        if(!dataStore.load(teamFormationIndex)) return null;
        return defenseTeam(teamFormationIndex.teamId).load(dataStore,teamFormationIndex);
    }

    public BattleTeam defenseTeam(long defenseTeamId){
        BattleTeam defenseTeam = new BattleTeam();
        defenseTeam.distributionId(defenseTeamId);
        dataStore.load(defenseTeam);
        return defenseTeam;
    }

    public BattleLogList battleLogList(Session session){
        BattleLogList battleLogList = new BattleLogList(battleHistory(session));
        //Mock data here
        return battleLogList;
    }

    private List<BattleLog> battleHistory(Session session){
        List<BattleLog> battleLogs = new ArrayList<>();
        PlayerBattleLogIndex logIndex = new PlayerBattleLogIndex();
        logIndex.distributionId(session.distributionId());
        battleHistory.createIfAbsent(logIndex,true);
        if(logIndex.battleId0>0){
            BattleLogIndex log = new BattleLogIndex();
            log.distributionId(logIndex.battleId0);
            if(battleHistory.load(log)){
                BattleLog battleLog = new BattleLog(log);
                battleLog.defenseTeam = defenseTeam(log.defenseTeamId);
                battleLog.offenseTeam = defenseTeam(log.offenseTeamId);
                battleLogs.add(battleLog);
            }
        }
        if(logIndex.battleId1>0){
            BattleLogIndex log = new BattleLogIndex();
            log.distributionId(logIndex.battleId1);
            if(battleHistory.load(log)){
                BattleLog battleLog = new BattleLog(log);
                battleLog.defenseTeam = defenseTeam(log.defenseTeamId);
                battleLog.offenseTeam = defenseTeam(log.offenseTeamId);
                battleLogs.add(battleLog);
            }
        }
        if(logIndex.battleId2>0){
            BattleLogIndex log = new BattleLogIndex();
            log.distributionId(logIndex.battleId2);
            if(battleHistory.load(log)){
                BattleLog battleLog = new BattleLog(log);
                battleLog.defenseTeam = defenseTeam(log.defenseTeamId);
                battleLog.offenseTeam = defenseTeam(log.offenseTeamId);
                battleLogs.add(battleLog);
            }
        }
        if(logIndex.battleId3>0){
            BattleLogIndex log = new BattleLogIndex();
            log.distributionId(logIndex.battleId3);
            if(battleHistory.load(log)){
                BattleLog battleLog = new BattleLog(log);
                battleLog.defenseTeam = defenseTeam(log.defenseTeamId);
                battleLog.offenseTeam = defenseTeam(log.offenseTeamId);
                battleLogs.add(battleLog);
            }
        }
        if(logIndex.battleId4>0){
            BattleLogIndex log = new BattleLogIndex();
            log.distributionId(logIndex.battleId4);
            if(battleHistory.load(log)){
                BattleLog battleLog = new BattleLog(log);
                battleLog.defenseTeam = defenseTeam(log.defenseTeamId);
                battleLog.offenseTeam = defenseTeam(log.offenseTeamId);
                battleLogs.add(battleLog);
            }
        }
        return battleLogs;
    }

    public void onLoaded(SeasonCredentialConfiguration loaded){
        long[] ix ={1};
        loaded.list().forEach(season ->{
            seasons.put(ix[0]++,season);//season order index
            seasons.put(season.distributionId(),season);
        });
        this.rotation.seasonRotation = loaded.distributionId();
        long delay = TimeUtil.expired(loaded.startTime())? 100 : TimeUtil.durationUTCMilliseconds(LocalDateTime.now(),loaded.startTime());
        serviceContext.schedule(new ScheduleRunner(delay,()->scheduleSeason()));
    }

    public void onRemoved(SeasonCredentialConfiguration removed){
        scheduleStore.mapRemove(removed.key().asBinary());
        this.rotation.seasonRotation = 0;
        if(this.rotation.scheduledFuture!=null) this.rotation.scheduledFuture.cancel(true);
        Configurable.Listener listener = configurableListeners.get(OnAccess.SEASON);
        if(listener==null) return;
        SeasonCredentialConfiguration.Season current = seasons.remove(CURRENT_SEASON_INDEX);
        if(current!=null){
            listener.onRemoved(current);
        }
        seasons.clear();
    }

    public SeasonCredentialConfiguration.Season currentSeason(){
        return seasons.get(CURRENT_SEASON_INDEX);// currentSeason from season rotation
    }

    public void onBattleEnd(BattleEndResult battleEndResult){
        GameEndEvent gameEndEvent = new GameEndEvent();
        gameEndEvent.offenseTeamId = battleEndResult.offenseTeamId;
        gameEndEvent.offenseEloLevel = battleEndResult.offenseEloLevelUpdated;
        gameEndEvent.defenseTeamId = battleEndResult.defenseTeamId;
        gameEndEvent.defenseEloLevel = battleEndResult.defenseEloLevelUpdated;
        this.serviceContext.eventService().publish(gameEndEvent);
        //generate battle log
        BattleLogIndex battleLog = new BattleLogIndex();
        battleLog.defenseTeamId = battleEndResult.defenseTeamId;
        battleLog.offenseTeamId = battleEndResult.offenseTeamId;
        battleLog.defenseEloGain = battleEndResult.defenseEloLevelDelta;
        battleLog.offenseEloGain = battleEndResult.offenseEloLevelDelta;
        battleLog.defenseElo = battleEndResult.defenseEloLevelUpdated;
        if(!battleHistory.create(battleLog)){
            logger.warn("Should be happening if disk space not full");
            return;
        }
        PlayerBattleLogIndex defenseLogIndex = new PlayerBattleLogIndex();
        defenseLogIndex.distributionId(battleEndResult.defensePlayerId);
        defenseLogIndex.dataStore(battleHistory);
        battleHistory.createIfAbsent(defenseLogIndex,true);
        defenseLogIndex.update(battleLog.distributionId());

        PlayerBattleLogIndex offenseLogIndex = new PlayerBattleLogIndex();
        offenseLogIndex.distributionId(battleEndResult.defensePlayerId);
        offenseLogIndex.dataStore(battleHistory);
        battleHistory.createIfAbsent(offenseLogIndex,true);
        offenseLogIndex.update(battleLog.distributionId());

    }

    private void startSeason(SeasonRuntime seasonRuntime){
        LocalDateTime endTime = TimeUtil.fromUTCMilliseconds(seasonRuntime.endTime);
        long endTimeDuration = TimeUtil.expired(endTime)? 100 : TimeUtil.durationUTCMilliseconds(LocalDateTime.now(),TimeUtil.fromUTCMilliseconds(seasonRuntime.endTime));
        this.rotation.schedule(seasonRuntime);
        this.rotation.scheduledFuture = serviceContext.schedule(new ScheduleRunner(endTimeDuration,()->{
            endSeason();
        }));
        Configurable.Listener listener = configurableListeners.get(OnAccess.SEASON);
        if(listener==null){
            logger.warn("No game module listener available");
            return;
        }
        SeasonCredentialConfiguration.Season season = seasons.get(seasonRuntime.currentSeason);
        seasons.put(CURRENT_SEASON_INDEX,season);
        listener.onLoaded(season);
    }

    private void endSeason(){
        if(rotation.seasonRotation==0) return;
        byte[] lockKey = SnowflakeKey.from(rotation.seasonRotation).asBinary();
        try{
            scheduleStore.mapLock(lockKey);
            scheduleStore.mapRemove(lockKey);
            //do end first
            //start next if any
            SeasonCredentialConfiguration.Season next = seasons.get(rotation.sequence+1);
            SeasonRuntime seasonRuntime = new SeasonRuntime();
            seasonRuntime.distributionId(rotation.seasonRotation);
            dataStore.createIfAbsent(seasonRuntime,true);
            if(next==null){
                seasonRuntime.currentSeason=0;
                seasonRuntime.endTime = 0;
                seasonRuntime.ended = true;
            }
            else{
                seasonRuntime.currentSeason = next.seasonId;
                seasonRuntime.endTime = TimeUtil.toUTCMilliseconds(LocalDateTime.now().plusDays(seasonRunningDays).plusMinutes(seasonTimeGap));
                scheduleStore.mapSet(lockKey,seasonRuntime.toBinary());
            }
            dataStore.update(seasonRuntime);
        }catch (Exception ex){
            logger.error("Error on end season",ex);
        }
        finally {
            scheduleStore.mapUnlock(lockKey);
        }
        scheduleSeason();
    }

    private void scheduleSeason(){
        if(rotation.seasonRotation==0) return;
        byte[] lockKey = SnowflakeKey.from(rotation.seasonRotation).asBinary();
        try{
            scheduleStore.mapLock(lockKey);
            SeasonRuntime seasonRuntime = new SeasonRuntime();
            byte[] clusterCache = scheduleStore.mapGet(lockKey);
            if(clusterCache != null){
                seasonRuntime.fromBinary(clusterCache);
                startSeason(seasonRuntime);
                return;
            }
            //initialize season rotation
            seasonRuntime.distributionId(this.rotation.seasonRotation);
            dataStore.createIfAbsent(seasonRuntime,true);
            if(seasonRuntime.ended){
                logger.warn("Season rotation is ended");
                return;
            }
            if(seasonRuntime.currentSeason==0){
                SeasonCredentialConfiguration.Season startSeason = seasons.get(1);
                seasonRuntime.sequence = 1;
                seasonRuntime.currentSeason = startSeason.seasonId;
                seasonRuntime.endTime = TimeUtil.toUTCMilliseconds(LocalDateTime.now().plusDays(seasonRunningDays).plusMinutes(seasonTimeGap));
                dataStore.update(startSeason);
            }
            scheduleStore.mapSet(lockKey,seasonRuntime.toBinary());
            startSeason(seasonRuntime);
        }catch (Exception ex){
            logger.error("Error on season schedule",ex);
        }
        finally {
            scheduleStore.mapUnlock(lockKey);
        }
    }

    private List<Rating> findMatches(Session session){
        List<Rating> matches = new ArrayList<>();
        Rating rating = platformGameServiceProvider.presenceServiceProvider().rating(session);
        int[] size={0};
        matchMakingStore.backup().forEachEdgeKey(MatchMaking.rangedKey(rating.level()),"elo",(k, v)->{
            Rating match = platformGameServiceProvider.presenceServiceProvider().rating(new SimpleStub(v.readLong()));
            //to do filter out code
            matches.add(match);
            size[0]++;
            return size[0]<5;
        });
        return matches;
    }

    private TeamFormationIndex teamFormationIndex(long systemId){
        TeamFormationIndex teamFormationIndex = new TeamFormationIndex();
        teamFormationIndex.distributionId(systemId);
        dataStore.createIfAbsent(teamFormationIndex,true);
        return teamFormationIndex;
    }

    private void onEvent(Event event){
        if(event.getClassId()== PortableEventRegistry.GAME_END_EVENT_CID){
            GameEndEvent gameEndEvent =(GameEndEvent)event;
            if(gameEndEvent.offenseTeamId > 0) onMatchMakingPool(gameEndEvent.offenseTeamId,gameEndEvent.offenseEloLevel);
            if(gameEndEvent.defenseTeamId > 0) onMatchMakingPool(gameEndEvent.defenseTeamId,gameEndEvent.defenseEloLevel);
            return;
        }
        if(event.getClassId()==PortableEventRegistry.TEAM_FORMATION_EVENT_CID){
            TeamFormationEvent formationEvent = (TeamFormationEvent)event;
            if(formationEvent.distributionId() > 0) onMatchMakingPool(formationEvent.distributionId(), formationEvent.eloLevel);
        }
    }

    private void onMatchMakingPool(long systemId,int eloLevel){
        IntegerRangeKey integerKey = MatchMaking.rangedKey(eloLevel);
        matchMakingStore.backup().setEdge("elo",(k,v)->{
            integerKey.write(k);
            v.writeLong(systemId);
            return true;
        });
    }

    @Override
    public boolean onItemRegistered(String category,String itemId) {
        ConfigurableObject configurableObject = new ConfigurableObject();
        configurableObject.distributionKey(itemId);
        Descriptor app = gameCluster.serviceWithCategory(category);
        if(!applicationPreSetup.load(app,configurableObject)){
            logger.warn("League config not available");
            return false;
        }
        onLeague(configurableObject);
        return true;
    }

    @Override
    public boolean onItemReleased(String category,String itemId) {
        ConfigurableObject configurableObject = new ConfigurableObject();
        configurableObject.distributionKey(itemId);
        Descriptor app = gameCluster.serviceWithCategory(category);
        if(!applicationPreSetup.load(app,configurableObject)){
            logger.warn("League config not available");
            return false;
        }
        League removed = new League(configurableObject);
        for(int i=removed.startPoint();i<=removed.endPoint();i++){
            leagues.remove(i);
        }
        return true;
    }

    @Override
    public String registerConfigurableListener(Descriptor descriptor, Configurable.Listener listener) {
        List<ConfigurableObject> items = applicationPreSetup.list(descriptor,new ConfigurableObjectQuery(descriptor.key(),"League"));
        items.forEach((a)-> {
            if(!a.disabled()) {
                onLeague(a);
            }
        });
        return null;
    }

    private void onLeague(ConfigurableObject a){
        PostBattleReward postBattleReward = new PostBattleReward();
        postBattleReward.distributionId(a.application().get("PostBattleReward").getAsJsonArray().get(0).getAsLong());
        PlacementReward placementReward = new PlacementReward();
        placementReward.distributionId(a.application().get("PlacementReward").getAsJsonArray().get(0).getAsLong());
        LeagueReward leagueReward = new LeagueReward();
        leagueReward.distributionId(a.application().get("LeagueReward").getAsJsonArray().get(0).getAsLong());
        a.configurableSetting(gameCluster.configurableCategories(Configurable.APPLICATION_CONFIG_TYPE));
        League league = new League(a.setup());
        league.postBattleReward = postBattleReward;
        league.placementReward = placementReward;
        league.leagueReward = leagueReward;
        for(int i=league.startPoint();i<=league.endPoint();i++){
            leagues.put(i,league);
        }
    }

    public boolean isDefenseOnCooldown(long defenseTeamId){
        DefenseCooldown defenseCooldown = new DefenseCooldown();
        defenseCooldown.distributionId(defenseTeamId);
        if(!cooldownStore.load(defenseCooldown)) {
            return false;
        }
        else{
            return !TimeUtil.expired(TimeUtil.fromUTCMilliseconds(defenseCooldown.cooldownTimer));
        }
    }

    public void startDefenseCooldown(long defenseTeamId){
        DefenseCooldown defenseCooldown = new DefenseCooldown();
        defenseCooldown.distributionId(defenseTeamId);
        cooldownStore.createIfAbsent(defenseCooldown,true);
        defenseCooldown.cooldownTimer = TimeUtil.toUTCMilliseconds(LocalDateTime.now().plusMinutes(60));
        cooldownStore.update(defenseCooldown);
    }

    public String battleLogMockData(){
        return battleLogMockData.toString();
    }

}
