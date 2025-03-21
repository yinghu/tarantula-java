package com.tarantula.platform.presence.pvp;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.*;
import com.icodesoftware.util.*;
import com.tarantula.game.GameRating;
import com.tarantula.game.SimpleStub;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.configuration.SeasonCredentialConfiguration;
import com.tarantula.platform.event.PortableEventRegistry;
import com.tarantula.platform.item.Application;
import com.tarantula.platform.item.ConfigurableObject;
import com.tarantula.platform.item.ConfigurableObjectQuery;
import com.tarantula.platform.item.PlatformItemServiceProvider;
import com.tarantula.platform.presence.Profile;

import java.io.File;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


public class PlatformPVPBattleServiceProvider extends PlatformItemServiceProvider implements Configurable.Listener<SeasonCredentialConfiguration>{

    //NEVER PUSH TO REMOTE WITH TRUE
    private static final boolean DEV_CONFIG = false;
    private static final boolean COOL_DOWN_ENABLED = false;
    private static final boolean FORCE_BOT_CREATE = true;
    private static final long CURRENT_SEASON_INDEX = 0;
    public static final String NAME = "pvp_battle";

    // seconds dev sample config replaced with the pvp config section DEV_CONFIG = false
    private int teamCreationWaitingTime = 5;
    private int seasonTimeGap = 60;
    private int seasonRunningTime = 180;
    private int reMatchWaitingTime = 180;
    // seconds end
    private int championsLeaderBoardThreshold = 2050;
    private int championsLeaderBoardSize = 100;
    private int matchEloDifferenceThreshold = 1000;
    private int botFillEloThreshold = 300;
    private int coolDownTime = 60;

    private int matchMakingSnapshotSize = 100;
    private int matchMakingPoolSize = 100;
    private int matchMakingListSize = 5;
    private final AtomicInteger roundRobin = new AtomicInteger(0);
    private ConcurrentHashMap<Long, Application> rewardIndex = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, SeasonCredentialConfiguration.Season> seasons = new ConcurrentHashMap();
    private ConcurrentHashMap<Long,League> leagueConfigs = new ConcurrentHashMap<>();
    ConcurrentHashMap<Integer,League> leagues = new ConcurrentHashMap<>();
    private ClusterProvider.ClusterStore scheduleStore;

    private final SeasonRuntime rotation = new SeasonRuntime();

    private DataStore battleHistory;
    private DataStore playerRewardStore;
    private DataStore localMatchMakingStore;
    private DataStore localSeasonPlayerStore;

    private String gameEndTopic = GameEndEvent.GAME_END_TOPIC;

    private ConcurrentHashMap<IntegerKey,MatchMakingSnapshot> matchMakingSnapshot = new ConcurrentHashMap<>();

    //private List<BotIndex> botIndexList;
    private List<BattleTeam> bots;
    private ChampionLeaderBoard championLeaderBoard;
    private RNG rng = new JvmRNG();

    private TokenValidatorProvider tokenValidatorProvider;
    private final static String ANALYTICS_QUERY_HEADER = "#Analytics";
    private static String ANALYTICS_QUERY;
    public PlatformPVPBattleServiceProvider(PlatformGameServiceProvider gameServiceProvider){
        super(gameServiceProvider,NAME);
        ANALYTICS_QUERY = gameServiceProvider.gameCluster().typeId()+ANALYTICS_QUERY_HEADER;
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
            coolDownTime = pvp.get("defenseCooldownMinutes").getAsInt()*60;
        }
        this.championsLeaderBoardThreshold = pvp.get("championsLeaderBoardThreshold").getAsInt();
        this.botFillEloThreshold = pvp.get("botFillEloThreshold").getAsInt();
        this.matchMakingSnapshotSize = pvp.get("matchMakingSnapshotSize").getAsInt();
        this.matchMakingPoolSize = pvp.get("matchMakingPoolSize").getAsInt();
        this.dataStore = applicationPreSetup.dataStore(gameCluster,NAME+"_team_formation");
        this.playerRewardStore = applicationPreSetup.dataStore(gameCluster,NAME+"_player_reward");
        this.localMatchMakingStore = applicationPreSetup.localDataStore(gameCluster,NAME+"_match_making");
        this.localSeasonPlayerStore = applicationPreSetup.localDataStore(gameCluster,NAME+"_season_player");
        this.battleHistory = applicationPreSetup.dataStore(gameCluster,NAME+"_history");
        this.scheduleStore = this.serviceContext.clusterProvider().clusterStore(ClusterProvider.ClusterStore.SMALL,gameCluster.typeId()+"."+NAME);

        this.logger.warn("PVP battle service provider started on ->"+gameServiceName);
        this.serviceContext.eventService().registerEventListener(gameEndTopic,e->{
            onEvent(e);
            return true;
        });
        for(int i=0;i<matchMakingPoolSize;i++){
            IntegerKey mmPool = MatchMaking.pool(i);
            MatchMakingSnapshot matchMakingSnapshot = new MatchMakingSnapshot(matchMakingSnapshotSize);
            this.matchMakingSnapshot.put(mmPool,matchMakingSnapshot);
            localMatchMakingStore.list(new DefenseTeamIndexQuery(mmPool,DefenseTeamIndex.POOL_LABEL),(t)->
                matchMakingSnapshot.pending.offer(t)
            );
            logger.warn("Preloading match making list ["+matchMakingSnapshot.pending.size()+"] from pool ["+mmPool.key()+"]");
        }

        List<BotIndex> botIndexList = localSeasonPlayerStore.list(new BotIndexQuery(serviceContext.node().nodeId()));
        bots = new ArrayList<>();
        if(FORCE_BOT_CREATE){
            botIndexList.forEach(botIndex -> localSeasonPlayerStore.delete(botIndex));
            botIndexList.clear();
        }
        if(botIndexList.size()==0){
            List<String> dlist = new ArrayList<>();
            File f = new File("../conf/pvp/entryBots");
            if(f.exists()){
                for(String s : f.list()){
                    if(s.endsWith(".json")){
                        dlist.add(s);
                    }
                }
            }
            //SimpleStub bot = new SimpleStub(serviceContext.distributionId());
            SnowflakeKey ownerKey = SnowflakeKey.from(serviceContext.node().nodeId());
            dlist.forEach(js->{
                logger.warn("Creating bot from ["+js+"]");
                JsonObject formation = JsonUtil.parse(Thread.currentThread().getContextClassLoader().getResourceAsStream("pvp/entryBots/"+js));
                BotIndex created = saveBot(formation.toString().getBytes());
                created.ownerKey(ownerKey);
                localSeasonPlayerStore.create(created);
                botIndexList.add(saveBot(formation.toString().getBytes()));
            });
        }
        botIndexList.forEach(botIndex->{
            BattleTeam bot = new BattleTeam();
            bot.distributionId(botIndex.teamId);
            bot.load(dataStore, bot.distributionId());
            Profile botProfile = new Profile();
            botProfile.distributionId(bot.distributionId());
            if(!localSeasonPlayerStore.load(botProfile)){
                JsonObject profile = JsonUtil.parse(Thread.currentThread().getContextClassLoader().getResourceAsStream("pvp/profile/profile_"+bot.teamPower+".json"));
                botProfile.displayName = profile.get("DisplayName").getAsString();
                botProfile.iconIndex = profile.get("IconIndex").getAsInt();
                botProfile.profileSequence = rng.onNext(1000)+rng.onNext(100);
                localSeasonPlayerStore.createIfAbsent(botProfile,false);
            }
            bot.botProfile = botProfile.toJson();
            logger.warn(bot.botProfile.toString()+" : "+bot.distributionId());
            bots.add(bot);
        });
        this.platformGameServiceProvider.configurationServiceProvider().addConfigurableListener(OnAccess.SEASON,this);
        this.tokenValidatorProvider = (TokenValidatorProvider) serviceContext.serviceProvider(TokenValidatorProvider.NAME);
    }

    public ChampionLeaderBoard championLeaderBoard(){
        return championLeaderBoard != null ? championLeaderBoard : ChampionLeaderBoard.noBoard();
    }

    public MatchMaking forceMatchMaking(Session session){
        onMatchmakingAnalytic(session.distributionId(), Integer.parseInt(session.name()));
        voidMatchMakingTimer(session);
        return matchMaking(session);
    }

    private void voidMatchMakingTimer(Session session){
        MatchMakingIndex matchMakingIndex = new MatchMakingIndex();
        matchMakingIndex.distributionId(session.distributionId());
        localMatchMakingStore.createIfAbsent(matchMakingIndex,true);
        matchMakingIndex.timestamp(0);
        localMatchMakingStore.update(matchMakingIndex);
    }

    public MatchMaking matchMaking(Session session){

        TeamFormationIndex teamFormationIndex = teamFormationIndex(session.distributionId());

        if(teamFormationIndex.teamId==0){
            return MatchMaking.failure(PvpErrorCode.NO_TEAM_FORMATION,"no defense team created");
        }
        MatchMakingIndex matchMakingIndex = new MatchMakingIndex();
        matchMakingIndex.distributionId(session.distributionId());
        localMatchMakingStore.createIfAbsent(matchMakingIndex,true);
        List<DefenseTeamIndex> pending;
        if(!matchMakingIndex.expired()){
            pending = matchMakingIndex.list(localMatchMakingStore);
        }
        else{
            pending = findMatches(session,matchMakingIndex);
        }
        Rating attackerRating = platformGameServiceProvider.presenceServiceProvider().rating(session);
        BattleTeam attackersDefenseTeam = defenseTeam(attackerRating);

        List<BattleTeam> matches = new ArrayList<>();
        pending.forEach(defenseTeamIndex -> {
            BattleTeam defenseTeam = assembly(defenseTeamIndex.teamId());
            if(defenseTeam != null){
                Rating defenderRating = this.platformGameServiceProvider.presenceServiceProvider().rating(new SimpleStub(defenseTeamIndex.playerId));
                defenseTeam.elo = defenderRating.level();
                BattleLogIndex battleLogIndex = new BattleLogIndex();
                battleLogIndex.playerId = session.distributionId();
                battleLogIndex.defenseTeamId = defenseTeamIndex.teamId();
                if(battleHistory.load(battleLogIndex)) {
                    defenseTeam.battled = true;
                    defenseTeam.battlePoint = battleLogIndex.offenseEloGain;
                }

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
        if(matches.size()==0) voidMatchMakingTimer(session); //always allow client to retry mm-list to find matches
        if(matches.size()< matchMakingListSize && attackerRating.level() < botFillEloThreshold){
            fillBots(session,attackersDefenseTeam,matches);
        }
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
        defenseTeam.saveAsDefense(dataStore,teamFormationIndex,teamCreationWaitingTime);
        this.serviceContext.eventService().publish(new TeamFormationEvent(session.distributionId(),defenseTeam.distributionId()));
        onSaveDefenseAnalytic(defenseTeam);
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
        BattleTeam offenseTeam = BattleTeam.parse(content);
        offenseTeam.playerId = session.distributionId();
        if(!offenseTeam.saveAsOffense(dataStore)){
            return TeamFormationResponse.retryOffenseTeam();
        }
        return TeamFormationResponse.responseOnOffenseTeam(offenseTeam.distributionId());
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

    public void rewardGranted(Session session){
        long rewardId = Long.parseLong(session.name());
        PlayerRewardIndex playerRewardIndex = playerRewardIndex(session.distributionId());
        if(playerRewardIndex.postBattleRewardId == rewardId){
            playerRewardIndex.postBattleRewardId = 0;
        }
        if(playerRewardIndex.placementRewardId == rewardId){
            playerRewardIndex.placementRewardId = 0;
        }
        if(playerRewardIndex.leagueRewardId == rewardId){
            playerRewardIndex.leagueRewardId = 0;
        }
        playerRewardIndex.update();
    }

    private BattleTeam assembly(long teamId){
        BattleTeam battleTeam = new BattleTeam();
        return battleTeam.load(dataStore,teamId);
    }


    private List<BattleLog> battleHistory(Session session){
        List<BattleLog> battleLogs = new ArrayList<>();
        PlayerBattleLogIndex logIndex = new PlayerBattleLogIndex();
        logIndex.distributionId(session.distributionId());
        battleHistory.createIfAbsent(logIndex,true);

        for(long battleId: logIndex.getOffenseBattles()){
            addToBattleLog(session.distributionId(),battleLogs, battleId);
        }

        for(long battleId: logIndex.getDefenseBattles()){
            addToBattleLog(session.distributionId(),battleLogs, battleId);
        }

        return battleLogs;
    }

    private void addToBattleLog(long playerId,List<BattleLog> battleLogs, long battleId) {
        if(playerId ==0 || battleId == 0) return;
        BattleLogIndex log = new BattleLogIndex();
        log.playerId = playerId;
        log.defenseTeamId = battleId;
        if(battleHistory.load(log)){
            BattleLog battleLog = new BattleLog(log);
            battleLog.defenseTeam = assembly(log.defenseTeamId);
            battleLog.offenseTeam = assembly(log.offenseTeamId);
            if(battleLog.defenseTeam.teamType == BattleTeam.TeamType.BOT){
                battleLog.defenseTeam.botProfile = botProfile(log.defenseTeamId).toJson();
            }
            battleLogs.add(battleLog);
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

    public void setELO(long playerID, int newELO){
        GameRating gameRating = platformGameServiceProvider.presenceServiceProvider().rating(new SimpleStub(playerID));
        gameRating.level(newELO);
        gameRating.update();

        PlayerRewardIndex playerRewardIndex = playerRewardIndex(playerID);
        League league = leagues.get(newELO);
        if(league!=null){
            playerRewardIndex.postBattleRewardId = league.postBattleReward.distributionId();
            playerRewardIndex.update();
        }
    }

    public void onBattleEnd(BattleEndResult battleEndResult){
        GameEndEvent gameEndEvent = new GameEndEvent();
        gameEndEvent.offensePlayerId = battleEndResult.offensePlayerId;
        gameEndEvent.offenseTeamId = battleEndResult.offenseTeamId;
        gameEndEvent.offenseEloLevel = battleEndResult.offenseEloLevelUpdated;
        gameEndEvent.offenseEloLevelDelta = battleEndResult.offenseEloLevelDelta;
        gameEndEvent.defensePlayerId = battleEndResult.defensePlayerId;
        gameEndEvent.defenseTeamId = battleEndResult.defenseTeamId;
        gameEndEvent.defenseEloLevel = battleEndResult.defenseEloLevelUpdated;
        gameEndEvent.defenseEloLevelDelta = battleEndResult.defenseEloLevelDelta;

        this.serviceContext.eventService().publish(gameEndEvent);
        if(battleEndResult.offenseEloLevelDelta>0){
            PlayerRewardIndex playerRewardIndex = playerRewardIndex(battleEndResult.offensePlayerId);
            League league = leagues.get(battleEndResult.offenseEloLevelUpdated);
            if(league!=null){
                playerRewardIndex.postBattleRewardId = league.postBattleReward.distributionId();
                playerRewardIndex.update();
            }
        }
        updateBattleLogIndex(battleEndResult,true);
        updateBattleLogIndex(battleEndResult,false);
        //logger.warn("OFFENSE : "+battleEndResult.offensePlayerId+" : "+battleEndResult.offenseEloLevelUpdated+" : "+battleEndResult.offenseEloLevelDelta);
        //logger.warn("DEFENSE : "+battleEndResult.defensePlayerId+" : "+battleEndResult.defenseEloLevelUpdated+" : "+battleEndResult.defenseEloLevelDelta);

        onRatingChangeAnalytics(battleEndResult.offensePlayerId, battleEndResult.offenseEloLevelDelta, battleEndResult.offenseEloLevelUpdated, battleEndResult.battleId, true);
        onRatingChangeAnalytics(battleEndResult.defensePlayerId, battleEndResult.defenseEloLevelDelta, battleEndResult.defenseEloLevelUpdated, battleEndResult.battleId, false);
    }

    private void updateBattleLogIndex(BattleEndResult battleEndResult,boolean offense){
        BattleLogIndex battleLog = new BattleLogIndex();
        battleLog.playerId = offense? battleEndResult.offensePlayerId : battleEndResult.defensePlayerId;
        battleLog.defenseTeamId = battleEndResult.defenseTeamId;
        battleHistory.createIfAbsent(battleLog,true);
        battleLog.offenseTeamId = battleEndResult.offenseTeamId;
        battleLog.defenseEloGain = battleEndResult.defenseEloLevelDelta;
        battleLog.offenseEloGain = battleEndResult.offenseEloLevelDelta;
        battleLog.defenseElo = battleEndResult.defenseEloLevelUpdated;
        battleLog.offenseElo = battleEndResult.offenseEloLevelUpdated;
        battleHistory.update(battleLog); //overriding previous if same defense team

        PlayerBattleLogIndex playerLogIndex = new PlayerBattleLogIndex();
        playerLogIndex.distributionId(offense? battleEndResult.offensePlayerId : battleEndResult.defensePlayerId);
        playerLogIndex.dataStore(battleHistory);
        battleHistory.createIfAbsent(playerLogIndex,true);
        if(offense) {
            playerLogIndex.updateOffenseLogs(battleLog.distributionId());
        }
        else {
            playerLogIndex.updateDefenseLogs(battleLog.distributionId());
        }
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
        this.championLeaderBoard = new ChampionLeaderBoard(localSeasonPlayerStore,serviceContext.node().nodeId(),season.seasonId,championsLeaderBoardThreshold,championsLeaderBoardSize);
        this.championLeaderBoard.load();
        seasons.put(CURRENT_SEASON_INDEX,season);
        season.timestamp(TimeUtil.toUTCMilliseconds(endTime));
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
        SeasonCredentialConfiguration.Season ended = currentSeason();

        onSeasonListener(ended,true);
        seasons.remove(CURRENT_SEASON_INDEX);
        byte[] lockKey = SnowflakeKey.from(rotation.seasonRotation).asBinary();
        try{
            scheduleStore.mapLock(lockKey);
            if(scheduleStore.mapRemove(lockKey)==null){
                logger.warn("Season end processing on other nodes");
            }else{
                logger.warn("Processing season end ["+rotation.sequence+"]["+rotation.currentSeason+"]");
                //do end first
                placementReward(ended.seasonId);
                leagueReward(ended.seasonId);
                //start next if any
                SeasonCredentialConfiguration.Season next = seasons.get(rotation.sequence+1);
                SeasonRuntime seasonRuntime = new SeasonRuntime();
                seasonRuntime.distributionId(rotation.seasonRotation);
                dataStore.createIfAbsent(seasonRuntime,true);
                onSeasonChangeAnalytic(ended, next);
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

    public boolean isCurrentDefenseTeam(long teamId, long playerId){
        return teamId == currentDefenseTeam(new SimpleStub(playerId)).distributionId();
    }

    private List<DefenseTeamIndex> findMatches(Session session,MatchMakingIndex matchMakingIndex){
        //logger.warn("Finding matches ["+matchMakingIndex.poolIndex+"]");
        Rating playerElo = platformGameServiceProvider.presenceServiceProvider().rating(session);
        IntegerKey mkey = MatchMaking.pool(matchMakingIndex.poolIndex);
        MatchMakingSnapshot snapshot = matchMakingSnapshot.putIfAbsent(mkey,new MatchMakingSnapshot(matchMakingSnapshotSize));
        List<DefenseTeamIndex> temp = snapshot.pending.stream().toList();
        for(DefenseTeamIndex match : temp){
            if(match.playerId != session.distributionId()){
                localMatchMakingStore.load(match);
                if((match.onCooldown() && COOL_DOWN_ENABLED) || !isCurrentDefenseTeam(match.distributionId(), match.playerId)) continue;
                Rating matchElo = platformGameServiceProvider.presenceServiceProvider().rating(new SimpleStub(match.playerId));
                if(matchElo.level() > playerElo.level() && matchElo.level()-playerElo.level() < matchEloDifferenceThreshold){
                    if(matchMakingIndex.higher(match)) break;
                }
                if(matchElo.level() < playerElo.level() && playerElo.level()- matchElo.level() < matchEloDifferenceThreshold){
                    if(matchMakingIndex.lower(match)) break;
                }
            }
        }
        matchMakingIndex.poolIndex = (matchMakingIndex.poolIndex < matchMakingPoolSize-1) ? matchMakingIndex.poolIndex+1 : 0;
        matchMakingIndex.timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now().plusSeconds(reMatchWaitingTime)));
        matchMakingIndex.reset();
        localMatchMakingStore.update(matchMakingIndex);
        List<DefenseTeamIndex> matches = matchMakingIndex.list(localMatchMakingStore);
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
            OnPartition[] partitions = serviceContext.partitions();
            RoutingKey routingKey = serviceContext.eventService().routingKey(gameEndEvent.offenseTeamId,"user/presence");
            if(partitions[routingKey.routingNumber()].opening()){
                Rating defenseElo = platformGameServiceProvider.presenceServiceProvider().rating(new SimpleStub(gameEndEvent.defensePlayerId));
                defenseElo.level(defenseElo.level()+(gameEndEvent.defenseEloLevelDelta));
                defenseElo.update();
            }
            if(gameEndEvent.defenseEloLevelDelta<0){ //cooldown reset
                DefenseTeamIndex defenseTeamIndex = new DefenseTeamIndex();
                defenseTeamIndex.distributionId(gameEndEvent.defenseTeamId);
                if(localMatchMakingStore.load(defenseTeamIndex)){
                    defenseTeamIndex.timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now().plusSeconds(coolDownTime)));
                    localMatchMakingStore.update(defenseTeamIndex);
                }
            }
            SeasonCredentialConfiguration.Season season = currentSeason();
            if(season==null) return;
            SeasonPlayerIndex seasonPlayerIndex = new SeasonPlayerIndex();
            seasonPlayerIndex.playerId = gameEndEvent.offensePlayerId;
            seasonPlayerIndex.seasonId = season.distributionId();
            seasonPlayerIndex.ownerKey(SnowflakeKey.from(seasonPlayerIndex.seasonId));
            localSeasonPlayerStore.createIfAbsent(seasonPlayerIndex,true);
            seasonPlayerIndex.timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
            localSeasonPlayerStore.update(seasonPlayerIndex);
            championLeaderBoard.onBoard(gameEndEvent.offensePlayerId,gameEndEvent.offenseEloLevel);
            return;
        }
        if(event.getClassId()==PortableEventRegistry.TEAM_FORMATION_EVENT_CID){
            TeamFormationEvent formationEvent = (TeamFormationEvent)event;
            if(formationEvent.stub() > 0 & formationEvent.distributionId() > 0){
                onMatchMakingPool(formationEvent.stub(),formationEvent.distributionId());
            }
        }
    }

    private void onMatchMakingPool(long playerId,long teamId){
        IntegerKey integerKey = MatchMaking.pool(roundRobin.getAndUpdate(v->{
            if(v<matchMakingPoolSize-1) return v+1;
            return 0;
        }));
        DefenseTeamIndex battleTeamIndex = new DefenseTeamIndex(integerKey);
        battleTeamIndex.playerId = playerId;
        battleTeamIndex.distributionId(teamId);
        localMatchMakingStore.createIfAbsent(battleTeamIndex,false);
        battleTeamIndex.ownerKey(SnowflakeKey.from(playerId));
        localMatchMakingStore.createEdge(battleTeamIndex,DefenseTeamIndex.PLAYER_LABEL);

        MatchMakingSnapshot snapshot = matchMakingSnapshot.putIfAbsent(integerKey,new MatchMakingSnapshot(matchMakingSnapshotSize));
        if(!snapshot.pending.offer(battleTeamIndex)){
            snapshot.pending.poll();//kick out first
            snapshot.pending.offer(battleTeamIndex);
        }
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

    public PlayerRewardIndex playerRewardIndex(long playerId){
        PlayerRewardIndex playerRewardIndex = new PlayerRewardIndex(playerId);
        playerRewardIndex.dataStore(playerRewardStore);
        playerRewardStore.createIfAbsent(playerRewardIndex,true);
        return playerRewardIndex;
    }


    private BotIndex saveBot(byte[] content){
        BattleTeam botTeam = BattleTeam.parse(content);
        //botTeam.ownerKey(SnowflakeKey.from(serviceContext.node().nodeId()));
        //botTeam.label("defense_bot");
        //botTeam.onEdge(true);
        botTeam.playerId = serviceContext.distributionId();
        botTeam.saveAsBot(dataStore);
        return new BotIndex(botTeam.distributionId());
    }

    private Profile botProfile(long botTeamId){
        Profile profile = new Profile();
        profile.distributionId(botTeamId);
        if(localSeasonPlayerStore.load(profile)) return profile;
        //in case no profile pre-vased
        profile.displayName = "AetherialAdventurer";
        profile.iconIndex = 0;
        profile.profileSequence = PvpErrorCode.NO_BOT_PROFILE;
        return profile;
    }

    private void fillBots(Session session,BattleTeam offenseTeam,List<BattleTeam> pending){
        int fill = matchMakingListSize - pending.size();
        int sz = bots.size();
        int[] rlist = rng.onNextList(sz,10); //can increase number of rng to reduce duplicate
        HashMap<Integer,Integer> marked = new HashMap<>();
        for(int x : rlist){
            if(!marked.containsKey(x)){
                BattleTeam bot = bots.get(x);
                setupBotBattleTeam(session,offenseTeam,bot);
                pending.add(bots.get(x));
                marked.put(x,x);
                fill--;
            }
            if(fill==0) break;
        }
        if(fill>0){ //should be very rare to here
            logger.warn("Ops you are not lucky to have duplicate numbers ["+fill+"]");
            for(int i=0;i<fill;i++){
                int ix = rng.onNext(sz);
                BattleTeam bot = bots.get(ix);
                setupBotBattleTeam(session,offenseTeam,bot);
                pending.add(bot);
            }
        }
    }

    private void setupBotBattleTeam(Session session,BattleTeam offenseTeam,BattleTeam defenseTeam){
        Rating attackerRating = this.platformGameServiceProvider.presenceServiceProvider().rating(session);
        Rating defenderRating = this.platformGameServiceProvider.presenceServiceProvider().rating(new SimpleStub(defenseTeam.playerId));
        defenseTeam.elo = defenderRating.level();
        BattleLogIndex battleLogIndex = new BattleLogIndex();
        battleLogIndex.playerId = session.distributionId();
        battleLogIndex.defenseTeamId = defenseTeam.distributionId();
        if(battleHistory.load(battleLogIndex)) {
            defenseTeam.battled = true;
            defenseTeam.battlePoint = battleLogIndex.offenseEloGain;
        }

        if(!defenseTeam.battled){
            int currentELO = attackerRating.level();
            PVPPointGenerator.updateELO(attackerRating, defenderRating, offenseTeam.teamPower, defenseTeam.teamPower, true);
            defenseTeam.winPointsEstimated = attackerRating.level() - currentELO;
            attackerRating.level(currentELO);

            PVPPointGenerator.updateELO(attackerRating, defenderRating,offenseTeam.teamPower, defenseTeam.teamPower, false);
            defenseTeam.losePointsEstimated = attackerRating.level() - currentELO;
            attackerRating.level(currentELO);
        }
    }



    private void placementReward(long seasonId){
        logger.warn("Season placement reward granting ["+seasonId+"]");
        ArrayBlockingQueue<SeasonPlayerIndex> pending  = new ArrayBlockingQueue(100);
        try{
            localSeasonPlayerStore.list(new SeasonPlayerIndexQuery(seasonId),ps->{
                if(!pending.offer(ps)){
                    ArrayList<SeasonPlayerIndex> drains = new ArrayList<>();
                    pending.drainTo(drains);
                    serviceContext.schedule(new ScheduleRunner(10,new PlacementScheduler(drains,this,this.platformGameServiceProvider.presenceServiceProvider())));
                    pending.clear();
                    pending.offer(ps);
                }
                return true;
            });
        }catch (Exception ex){
            logger.error("Unexpected error",ex);
        }
    }

    private void leagueReward(long seasonId) {
        logger.warn("Season league reward granting [" + seasonId + "]");
        ChampionLeaderBoard ldb = championLeaderBoard();
        serviceContext.schedule(new ScheduleRunner(10,new LeagueRewardScheduler(ldb.leaderBoard(),this,platformGameServiceProvider.presenceServiceProvider())));
    }

    //Analytics callback hook
    private void onMatchmakingAnalytic(long playerId, int currencyType){
        sendAnalytic(new PVPOpponentsRefreshAnalytic(playerId, currencyType).toBytes());
    }
    private void onSaveDefenseAnalytic(BattleTeam defenseTeam){
        sendAnalytic(new DefenseFormationSavedAnalytic(defenseTeam).toBytes());
    }

    private void onSeasonChangeAnalytic(SeasonCredentialConfiguration.Season oldSeason, SeasonCredentialConfiguration.Season newSeason){
        sendAnalytic(new PVPSeasonChangeAnalytic(oldSeason, newSeason).toBytes());
    }

    private void onRatingChangeAnalytics(long playerId, int eloDelta, int eloUpdated, long battleId, boolean attacking){
        int oldELO = eloUpdated + eloDelta;

        String oldLeague = leagues.get(oldELO).name();
        String currentLeague = leagues.get(eloUpdated).name();

        if(!oldLeague.equals(currentLeague)){
            sendAnalytic(new PVPLeagueChangeAnalytic(playerId, oldLeague, currentLeague, eloUpdated, battleId).toBytes());
        }

        sendAnalytic(new PVPRatingChangeAnalytic(playerId, eloDelta, eloUpdated, currentLeague, battleId, attacking).toBytes());
    }

    private void sendAnalytic(byte [] content){
        TokenValidatorProvider.AuthVendor webhook = tokenValidatorProvider.authVendor(OnAccess.WEB_HOOK);
        serviceContext.schedule(new ScheduleRunner(100,()->{
            webhook.upload(ANALYTICS_QUERY,content);
        }));
    }

    //end of analytics


}
