package com.tarantula.platform.presence.pvp;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.ScheduleRunner;
import com.icodesoftware.util.SnowflakeKey;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.configuration.SeasonCredentialConfiguration;
import com.tarantula.platform.item.PlatformItemServiceProvider;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class PlatformPVPBattleServiceProvider extends PlatformItemServiceProvider implements Configurable.Listener<SeasonCredentialConfiguration> {

    private static final long CURRENT_SEASON_INDEX = 0;
    public static final String NAME = "pvp_battle";
    private int teamCreationWaitingTime = 5;
    private int seasonTimeGap = 10; //minutes
    private int seasonRunningDays = 12; //days
    private ConcurrentHashMap<Long, SeasonCredentialConfiguration.Season> seasons = new ConcurrentHashMap();
    private ClusterProvider.ClusterStore scheduleStore;

    private final SeasonRuntime rotation = new SeasonRuntime();

    private DataStore battleHistory;

    public PlatformPVPBattleServiceProvider(PlatformGameServiceProvider gameServiceProvider){
        super(gameServiceProvider,NAME);
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        super.setup(serviceContext);
        Configuration configuration = serviceContext.configuration("game-presence-settings");
        JsonObject pvp = ((JsonElement)configuration.property("pvp")).getAsJsonObject();
        teamCreationWaitingTime = pvp.get("waitingMinutesPerTeamFormation").getAsInt();
        seasonTimeGap = pvp.get("seasonTimeGapMinutes").getAsInt();
        seasonRunningDays = pvp.get("seasonRunningDays").getAsInt();
        this.dataStore = applicationPreSetup.dataStore(gameCluster,NAME);
        this.battleHistory = applicationPreSetup.dataStore(gameCluster,NAME+"_history");
        this.scheduleStore = this.serviceContext.clusterProvider().clusterStore(ClusterProvider.ClusterStore.SMALL,gameCluster.typeId()+"."+NAME);
        this.logger = JDKLogger.getLogger(PlatformPVPBattleServiceProvider.class);
        this.logger.warn("PVP battle service provider started on ->"+gameServiceName);
        this.platformGameServiceProvider.configurationServiceProvider().addConfigurableListener(OnAccess.SEASON,this);
    }


    public List<DefenseTeam> matchMaking(Session session){
        ArrayList<DefenseTeam> matches = new ArrayList<>();
        findMatches(session).forEach(rating -> {
            DefenseTeam defenseTeam = defenseTeam(rating);
            if(defenseTeam != null){
                //calculate the estimated pvp points if the player wins.
                defenseTeam.winPointsEstimated = 100;
                matches.add(defenseTeam);
            }
        });
        return matches;
    }

    public TeamFormationResponse saveDefenseTeam(Session session,byte[] content){
        TeamFormationIndex teamFormationIndex = new TeamFormationIndex();
        teamFormationIndex.distributionId(session.distributionId());
        dataStore.createIfAbsent(teamFormationIndex,true);
        if(!teamFormationIndex.expired()) return TeamFormationResponse.failure(teamFormationIndex.timestamp());
        DefenseTeam defenseTeam = DefenseTeam.parse(content);
        defenseTeam.playerId = session.distributionId();
        defenseTeam.save(dataStore,teamFormationIndex,teamCreationWaitingTime);
        return TeamFormationResponse.success(teamFormationIndex.timestamp());
    }

    public TeamFormationResponse saveOffenseTeam(Session session,byte[] content){
        DefenseTeam defenseTeam = DefenseTeam.parse(content);
        defenseTeam.playerId = session.distributionId();
        dataStore.create(defenseTeam);
        return TeamFormationResponse.responseOnOffenseTeam(defenseTeam.distributionId());
    }

    public DefenseTeam defenseTeam(Rating rating){
        TeamFormationIndex teamFormationIndex = new TeamFormationIndex();
        teamFormationIndex.distributionId(rating.distributionId());
        if(!dataStore.load(teamFormationIndex)) return null;
        return defenseTeam(teamFormationIndex.teamId);
    }

    public DefenseTeam defenseTeam(long defenseTeamId){
        DefenseTeam defenseTeam = new DefenseTeam();
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

    public void onBattleEnd(){

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
        return matches;
    }




}
