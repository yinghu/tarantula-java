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
import com.tarantula.platform.item.Application;
import com.tarantula.platform.item.ConfigurableObject;
import com.tarantula.platform.item.ConfigurableObjectQuery;
import com.tarantula.platform.item.PlatformItemServiceProvider;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class PlatformPVPBattleServiceProvider extends PlatformItemServiceProvider implements Configurable.Listener<SeasonCredentialConfiguration>{

    //NEVER PUSH TO REMOTE WITH TRUE
    private static final boolean DEV_CONFIG = false;

    private static final long CURRENT_SEASON_INDEX = 0;
    public static final String NAME = "pvp_battle";
    public static final String MM_LABEL = "elo";
    // seconds dev sample config replaced with the pvp config section DEV_CONFIG = false
    private int teamCreationWaitingTime = 5;
    private int seasonTimeGap = 60;
    private int seasonRunningTime = 180;
    private int reMatchWaitingTime = 180;
    // seconds end
    private int championsLeaderBoardThreshold = 2050;
    private int matchMakingSnapshotSize = 100;
    private ConcurrentHashMap<Long, Application> rewardIndex = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, SeasonCredentialConfiguration.Season> seasons = new ConcurrentHashMap();
    private ConcurrentHashMap<Long,League> leagueConfigs = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer,League> leagues = new ConcurrentHashMap<>();
    private ClusterProvider.ClusterStore scheduleStore;

    private final SeasonRuntime rotation = new SeasonRuntime();

    private DataStore battleHistory;
    private DataStore playerStateStore;
    private DataStore matchMakingStore;
    private String gameEndTopic = GameEndEvent.GAME_END_TOPIC;

    private ConcurrentHashMap<IntegerRangeKey,MatchMakingSnapshot> matchMakingSnapshot = new ConcurrentHashMap<>();

    public PlatformPVPBattleServiceProvider(PlatformGameServiceProvider gameServiceProvider){
        super(gameServiceProvider,NAME);
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        super.setup(serviceContext);
        this.logger = JDKLogger.getLogger(PlatformPVPBattleServiceProvider.class);
        Configuration configuration = serviceContext.configuration("game-presence-settings");
        JsonObject pvp = ((JsonElement)configuration.property("pvp")).getAsJsonObject();
        if(!DEV_CONFIG){
            logger.warn("Loading configuration values to override dev values");
            teamCreationWaitingTime = pvp.get("waitingMinutesPerTeamFormation").getAsInt()*60; //to seconds
            seasonTimeGap = pvp.get("seasonTimeGapMinutes").getAsInt()*60; //to seconds
            seasonRunningTime= pvp.get("seasonRunningDays").getAsInt()*24*60*60; //to seconds
            reMatchWaitingTime = pvp.get("reMatchWaitingTimeMinutes").getAsInt()*60; //to seconds
        }
        championsLeaderBoardThreshold = pvp.get("championsLeaderBoardThreshold").getAsInt();
        this.matchMakingSnapshotSize = pvp.get("matchMakingSnapshotSize").getAsInt();
        this.dataStore = applicationPreSetup.dataStore(gameCluster,NAME);
        this.playerStateStore = applicationPreSetup.dataStore(gameCluster,NAME+"_player_state");
        this.matchMakingStore = applicationPreSetup.localDataStore(gameCluster,NAME+"_match_making");
        this.battleHistory = applicationPreSetup.dataStore(gameCluster,NAME+"_history");
        this.scheduleStore = this.serviceContext.clusterProvider().clusterStore(ClusterProvider.ClusterStore.SMALL,gameCluster.typeId()+"."+NAME);

        this.logger.warn("PVP battle service provider started on ->"+gameServiceName);
        this.serviceContext.eventService().registerEventListener(gameEndTopic,e->{
            onEvent(e);
            return true;
        });
        this.matchMakingSnapshot.put(MatchMaking.B0_100,new MatchMakingSnapshot(matchMakingSnapshotSize));
        matchMakingStore.backup().forEachEdgeKey(MatchMaking.B0_100,MM_LABEL,(k,v)->{
            MatchMakingSnapshot b0_100 = matchMakingSnapshot.get(MatchMaking.B0_100);
            b0_100.pending.offer(v.readLong());
            return true;
        });
        //to do preload mm-snapshot
        this.platformGameServiceProvider.configurationServiceProvider().addConfigurableListener(OnAccess.SEASON,this);
    }


    public MatchMaking matchMaking(Session session){
        TeamFormationIndex teamFormationIndex = teamFormationIndex(session.distributionId());

        if(teamFormationIndex.teamId==0){
            return MatchMaking.failure(PvpErrorCode.NO_TEAM_FORMATION,"no defense team created");
        }

        List<BattleTeam> matches = new ArrayList<>();
        List<BattleLog> offenseHistory = offenseHistory(session);
        Rating attackerRating = platformGameServiceProvider.presenceServiceProvider().rating(session);
        BattleTeam attackersDefenseTeam = defenseTeam(attackerRating);

        findMatches(session).forEach(defenderRating -> {
            BattleTeam defenseTeam = defenseTeam(defenderRating);

            if(defenseTeam != null){
                defenseTeam.elo = defenderRating.level();

                offenseHistory.forEach(battleLog -> {
                    if(battleLog.defenseTeam.distributionId() == defenseTeam.distributionId()) {
                        defenseTeam.battled = true;
                        defenseTeam.battlePoint = battleLog.offenseEloGain;
                    }
                });

                if(!defenseTeam.battled){
                    int currentELO = attackerRating.level();
                    PVPPointGenerator.updateELO(attackerRating, defenderRating, attackersDefenseTeam.teamPower, defenseTeam.teamPower, true);
                    defenseTeam.winPointsEstimated = attackerRating.level() - currentELO;
                    attackerRating.level(currentELO);

                    PVPPointGenerator.updateELO(attackerRating, defenderRating, attackersDefenseTeam.teamPower, defenseTeam.teamPower, false);
                    defenseTeam.losePointsEstimated = attackerRating.level() - currentELO;
                    attackerRating.level(currentELO);
                }

                matches.add(defenseTeam);
            }
        });
        MatchMaking matchMaking = MatchMaking.success(teamFormationIndex.timestamp(),matches);
        PlayerRewardIndex playerRewardIndex = playerRewardIndex(session.distributionId());
        Application postReward = rewardIndex.get(playerRewardIndex.postBattleRewardId);
        if(postReward==null) return matchMaking;
        matchMaking.postBattleReward = postReward;
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

    public BattleTeam currentDefenseTeam(Session session){
        TeamFormationIndex teamFormationIndex = new TeamFormationIndex();
        teamFormationIndex.distributionId(session.distributionId());
        dataStore.createIfAbsent(teamFormationIndex,true);
        if(teamFormationIndex.teamId>0) return assembly(teamFormationIndex.teamId);
        return new BattleTeam();
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
        return battleLogList;
    }

    public RewardList rewardList(Session session){
        RewardList rewardList = new RewardList();
        PlayerRewardIndex playerRewardIndex = playerRewardIndex(session.distributionId());
        if(playerRewardIndex.placementRewardId > 0){
            rewardList.placementReward = rewardIndex.get(playerRewardIndex.placementRewardId);
        }
        if(playerRewardIndex.leagueRewardId > 0){
            rewardList.leagueReward = rewardIndex.get(playerRewardIndex.leagueRewardId);
        }
        return  rewardList;
    }

    private BattleTeam assembly(long teamId){
        BattleTeam battleTeam = new BattleTeam();
        return battleTeam.load(dataStore,teamId);
    }

    private List<BattleLog> offenseHistory(Session session){
        List<BattleLog> battleLogs = new ArrayList<>();
        PlayerBattleLogIndex logIndex = new PlayerBattleLogIndex();
        logIndex.distributionId(session.distributionId());
        battleHistory.createIfAbsent(logIndex,true);

        for(long battleId: logIndex.getOffenseBattles()){
            addToBattleLog(battleLogs, battleId);
        }

        return battleLogs;
    }

    private List<BattleLog> battleHistory(Session session){
        List<BattleLog> battleLogs = new ArrayList<>();
        PlayerBattleLogIndex logIndex = new PlayerBattleLogIndex();
        logIndex.distributionId(session.distributionId());
        battleHistory.createIfAbsent(logIndex,true);

        for(long battleId: logIndex.getOffenseBattles()){
            addToBattleLog(battleLogs, battleId);
        }

        for(long battleId: logIndex.getDefenseBattles()){
            addToBattleLog(battleLogs, battleId);
        }

        return battleLogs;
    }

    private void addToBattleLog(List<BattleLog> battleLogs, long battleId) {
        if(battleId > 0){
            BattleLogIndex log = new BattleLogIndex();
            log.distributionId(battleId);
            if(battleHistory.load(log)){
                BattleLog battleLog = new BattleLog(log);
                battleLog.defenseTeam = assembly(log.defenseTeamId);
                battleLog.offenseTeam = assembly(log.offenseTeamId);
                battleLogs.add(battleLog);
            }
        }
    }

    public void onLoaded(SeasonCredentialConfiguration loaded){
        long[] ix ={1};
        loaded.list().forEach(season ->{
            seasons.put(ix[0],season);//season order index
            seasons.put(season.distributionId(),season);
            logger.warn("Season installed on ["+ix[0]+"] "+season.seasonId);
            ix[0]++;
        });
        this.rotation.seasonRotation = loaded.distributionId();
        long delay = TimeUtil.expired(loaded.startTime())? 100 : TimeUtil.durationUTCMilliseconds(LocalDateTime.now(),loaded.startTime());
        serviceContext.schedule(new ScheduleRunner(delay,()->scheduleSeason()));
    }

    public void onRemoved(SeasonCredentialConfiguration removed){
        scheduleStore.mapRemove(removed.key().asBinary());
        this.rotation.seasonRotation = 0;
        if(this.rotation.scheduledFuture!=null) this.rotation.scheduledFuture.cancel(true);
        SeasonCredentialConfiguration.Season current = seasons.remove(CURRENT_SEASON_INDEX);
        onSeasonListener(current,true);
        seasons.clear();
    }

    public SeasonCredentialConfiguration.Season currentSeason(){
        SeasonCredentialConfiguration.Season season = seasons.get(CURRENT_SEASON_INDEX);// currentSeason from season rotation
        return season!=null ? season : new SeasonCredentialConfiguration.Season();
    }

    public void onBattleEnd(BattleEndResult battleEndResult){
        GameEndEvent gameEndEvent = new GameEndEvent();
        gameEndEvent.offenseTeamId = battleEndResult.offenseTeamId;
        gameEndEvent.offenseEloLevel = battleEndResult.offenseEloLevelUpdated;
        gameEndEvent.defenseTeamId = battleEndResult.defenseTeamId;
        gameEndEvent.defenseEloLevel = battleEndResult.defenseEloLevelUpdated;
        this.serviceContext.eventService().publish(gameEndEvent);
        if(battleEndResult.offenseEloLevelDelta>0){
            PlayerRewardIndex playerRewardIndex = playerRewardIndex(battleEndResult.offensePlayerId);
            League league = leagues.get(battleEndResult.offenseEloLevelUpdated);
            if(league!=null){
                playerRewardIndex.postBattleRewardId = league.postBattleReward.distributionId();
                playerRewardIndex.update();
            }
        }
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
        defenseLogIndex.updateDefenseLogs(battleLog.distributionId());

        PlayerBattleLogIndex offenseLogIndex = new PlayerBattleLogIndex();
        offenseLogIndex.distributionId(battleEndResult.offensePlayerId);
        offenseLogIndex.dataStore(battleHistory);
        battleHistory.createIfAbsent(offenseLogIndex,true);
        offenseLogIndex.updateOffenseLogs(battleLog.distributionId());
    }

    private void startSeason(SeasonRuntime seasonRuntime){
        LocalDateTime endTime = TimeUtil.fromUTCMilliseconds(seasonRuntime.endTime);
        long endTimeDuration = TimeUtil.expired(endTime)? 100 : TimeUtil.durationUTCMilliseconds(LocalDateTime.now(),TimeUtil.fromUTCMilliseconds(seasonRuntime.endTime));
        this.rotation.schedule(seasonRuntime);
        this.rotation.scheduledFuture = serviceContext.schedule(new ScheduleRunner(endTimeDuration,()->{
            endSeason();
        }));
        logger.warn("Season starting ["+seasonRuntime.currentSeason+" : "+seasonRuntime.sequence+"]");
        SeasonCredentialConfiguration.Season season = seasons.get(seasonRuntime.currentSeason);
        seasons.put(CURRENT_SEASON_INDEX,season);
        onSeasonListener(season,false);
    }

    private void onSeasonListener(SeasonCredentialConfiguration.Season updated,boolean ended){
        if(updated==null) return;
        Configurable.Listener listener = configurableListeners.get(OnAccess.SEASON);
        if(listener==null){
            logger.warn("No game module listener available");
            return;
        }
        if(ended){
            listener.onRemoved(updated);
            return;
        }
        listener.onLoaded(updated);
    }

    private void endSeason(){
        if(rotation.seasonRotation==0){
            logger.warn("No season rotation has scheduled");
            return;
        }
        onSeasonListener(currentSeason(),true);
        seasons.remove(CURRENT_SEASON_INDEX);
        byte[] lockKey = SnowflakeKey.from(rotation.seasonRotation).asBinary();
        try{
            scheduleStore.mapLock(lockKey);
            if(scheduleStore.mapRemove(lockKey)==null){
                logger.warn("Season end processing on other nodes");
            }else{
                logger.warn("Processing season end ["+rotation.sequence+"]["+rotation.currentSeason+"]");
                //do end first
                //start next if any
                SeasonCredentialConfiguration.Season next = seasons.get(rotation.sequence+1);
                SeasonRuntime seasonRuntime = new SeasonRuntime();
                seasonRuntime.distributionId(rotation.seasonRotation);
                dataStore.createIfAbsent(seasonRuntime,true);
                if(next==null){
                    logger.warn("No season rotation ["+rotation.seasonRotation+"]");
                    seasonRuntime.currentSeason=0;
                    seasonRuntime.endTime = 0;
                    seasonRuntime.ended = true;
                }
                else{
                    seasonRuntime.sequence++;
                    seasonRuntime.currentSeason = next.seasonId;
                    seasonRuntime.endTime = TimeUtil.toUTCMilliseconds(LocalDateTime.now().plusSeconds(seasonRunningTime).plusSeconds(seasonTimeGap));
                    scheduleStore.mapSet(lockKey,seasonRuntime.toBinary());
                }
                dataStore.update(seasonRuntime);
            }
        }catch (Exception ex){
            logger.error("Error on end season",ex);
        }
        finally {
            scheduleStore.mapUnlock(lockKey);
        }
        try{Thread.sleep(1000);}catch (Exception ex){} //waiting for clustering data update
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
                rotation.seasonRotation = 0;
                return;
            }
            if(seasonRuntime.currentSeason==0){
                SeasonCredentialConfiguration.Season startSeason = seasons.get(1L);
                seasonRuntime.sequence = 1;
                seasonRuntime.currentSeason = startSeason.seasonId;
                seasonRuntime.endTime = TimeUtil.toUTCMilliseconds(LocalDateTime.now().plusSeconds(seasonRunningTime).plusSeconds(seasonTimeGap));
                dataStore.update(seasonRuntime);
                logger.warn("Initializing season from ["+seasonRuntime.currentSeason+" : "+seasonRuntime.sequence+"]");
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
        //temp code to pull first 5 from single pooled.
        List<Rating> matches = new ArrayList<>();
        Rating rating = platformGameServiceProvider.presenceServiceProvider().rating(session);
        IntegerRangeKey mkey = MatchMaking.rangedKey(rating.level());
        MatchMakingSnapshot snapshot = matchMakingSnapshot.putIfAbsent(mkey,new MatchMakingSnapshot(matchMakingSnapshotSize));
        List<Long> temp = snapshot.pending.stream().toList();
        int msize = 5;
        for(long matchId : temp){
            if(matchId != session.distributionId()){
                Rating match = platformGameServiceProvider.presenceServiceProvider().rating(new SimpleStub(matchId));
                //to do filter out code
                matches.add(match);
                msize--;
            }
            if(msize==0) break;
        }
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
        matchMakingStore.backup().setEdge(MM_LABEL,(k,v)->{
            integerKey.write(k);
            v.writeLong(systemId);
            return true;
        });
        matchMakingSnapshot.putIfAbsent(integerKey,new MatchMakingSnapshot(matchMakingSnapshotSize)).pending.add(systemId);
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
        leagueConfigs.remove(configurableObject.distributionId());
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

    public LeagueList league(){
        ArrayList<League> list = new ArrayList<>();
        leagueConfigs.forEach((k,v)->list.add(v));
        return new LeagueList(list);
    }

    private void onLeague(ConfigurableObject a){
        a.configurableSetting(gameCluster.configurableCategories(Configurable.APPLICATION_CONFIG_TYPE));
        League league = new League(a.setup());
        JsonObject payload = league.toJson();
        PostBattleReward postBattleReward = new PostBattleReward(payload.get("_postBattleReward").getAsJsonObject());
        rewardIndex.put(postBattleReward.distributionId(),postBattleReward);
        PlacementReward placementReward = new PlacementReward(payload.get("_placementReward").getAsJsonObject());
        rewardIndex.put(placementReward.distributionId(),placementReward);
        LeagueReward leagueReward = new LeagueReward(payload.get("_leagueReward").getAsJsonObject());
        rewardIndex.put(leagueReward.distributionId(),leagueReward);
        league.postBattleReward = postBattleReward;
        league.placementReward = placementReward;
        league.leagueReward = leagueReward;
        leagueConfigs.put(a.distributionId(),league);
        for(int i=league.startPoint();i<=league.endPoint();i++){
            leagues.put(i,league);
        }
    }

    private PlayerRewardIndex playerRewardIndex(long playerId){
        PlayerRewardIndex playerRewardIndex = new PlayerRewardIndex(playerId);
        playerRewardIndex.dataStore(playerStateStore);
        playerStateStore.createIfAbsent(playerRewardIndex,true);
        return playerRewardIndex;
    }

    public DefenseCooldown defenseCooldown(long teamId){
        DefenseCooldown defenseCooldown = new DefenseCooldown(teamId);
        defenseCooldown.dataStore(playerStateStore);
        playerStateStore.createIfAbsent(defenseCooldown,true);
        return defenseCooldown;
    }


}
