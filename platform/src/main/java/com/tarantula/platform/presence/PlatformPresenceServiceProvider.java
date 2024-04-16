package com.tarantula.platform.presence;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.protocol.GameContext;
import com.icodesoftware.protocol.statistics.UserStatistics;
import com.icodesoftware.service.ServiceContext;

import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.ScheduleRunner;
import com.icodesoftware.util.SnowflakeKey;
import com.icodesoftware.util.TimeUtil;
import com.perfectday.games.earth8.data.PlayerDataTrack;
import com.perfectday.games.earth8.data.PlayerDataTrackQuery;
import com.tarantula.game.GamePortableRegistry;
import com.tarantula.game.GameRating;

import com.tarantula.game.Stub;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.game.service.PlatformGameServiceSetup;
import com.tarantula.platform.leaderboard.PlatformLeaderBoardProvider;

import com.tarantula.platform.presence.saves.*;

import com.tarantula.platform.util.RecoverableQuery;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PlatformPresenceServiceProvider extends PlatformGameServiceSetup {

    public static final String NAME = "presence";

    private int recentlyPlayListSize;
    private int friendListSize;

    private long syncIntervalSeconds;
    private PlayList recentlyPlayList;

    private AtomicInteger updates;
    private ScheduleRunner scheduleRunner;
    private PlatformLeaderBoardProvider platformLeaderBoardProvider;

    private DataStore mDataStore;

    public PlatformPresenceServiceProvider(PlatformGameServiceProvider gameServiceProvider){
        super(gameServiceProvider,NAME);
        updates = new AtomicInteger(0);
    }


    @Override
    public void start() throws Exception {
        this.recentlyPlayList = new PlayList(recentlyPlayListSize);
        this.recentlyPlayList.distributionId(this.gameCluster.distributionId());
        this.dataStore.createIfAbsent(this.recentlyPlayList,true);
        this.recentlyPlayList.dataStore(this.dataStore);
        this.scheduleRunner = new ScheduleRunner(syncIntervalSeconds*1000,()->{
            syncPlayList();
        });
        this.serviceContext.schedule(scheduleRunner);
    }

    @Override
    public void waitForData(){
        this.platformLeaderBoardProvider = platformGameServiceProvider.leaderBoardProvider();
    }
    @Override
    public void setup(ServiceContext serviceContext) {
        super.setup(serviceContext);
        Configuration configuration = serviceContext.configuration("game-presence-settings");
        JsonObject plist = ((JsonElement)configuration.property("playList")).getAsJsonObject();
        this.recentlyPlayListSize = plist.get("recentlyListSize").getAsInt();
        this.friendListSize = plist.get("friendListSize").getAsInt();
        this.syncIntervalSeconds = plist.get("syncIntervalSeconds").getAsLong();
        this.dataStore = this.applicationPreSetup.dataStore(gameCluster,NAME);
        this.mDataStore = this.applicationPreSetup.dataStore(gameCluster,NAME+"_mapping_object");
        this.logger = JDKLogger.getLogger(PlatformPresenceServiceProvider.class);
        this.logger.warn("Presence service provider started on ->"+gameServiceName);
    }
    public void onFriendList(String systemId,String friendSystemId){
        PlayList playList = new PlayList(friendListSize);
        playList.distributionKey(systemId);
        this.dataStore.createIfAbsent(playList,true);
        playList.playListIndex.push(friendSystemId);
        this.dataStore.update(playList);
    }
    public void onPlay(String systemId){
        this.recentlyPlayList.playListIndex.push(systemId);
        updates.incrementAndGet();
    }
    public List<String> friendList(String systemId){
        PlayList playList = new PlayList(friendListSize);
        playList.distributionKey(systemId);
        this.dataStore.createIfAbsent(playList,true);
        return playList.playListIndex.list(new ArrayList<>());
    }
    public List<String> recentlyPlayList(){
        return this.recentlyPlayList.playListIndex.list(new ArrayList<>());
    }

    public Profile profile(String systemId){
        Profile profile = new Profile();
        profile.displayName ="player";
        profile.distributionKey(systemId);
        this.dataStore.createIfAbsent(profile,true);
        profile.dataStore(this.dataStore);
        return profile;
    }

    public boolean createProfile(Session session){

        DataStore profileDataStore = applicationPreSetup.onDataStore("profile");
        var playerProfile = profileDataStore.list(new ProfileQuery(session.distributionId()));

        if(playerProfile.isEmpty()){
            Profile profile = new Profile();

            profile.configureAndValidate(session.payload());
            profile.ownerKey(SnowflakeKey.from(session.distributionId()));

            return profileDataStore.create(profile);
        }

        return false;
    }

    public ProfilePayload getProfilePayload(String IDs){
        String[] playerIDs = IDs.split("#");

        List<Profile> playerProfiles = new ArrayList<>();
        DataStore profileDataStore = applicationPreSetup.onDataStore("profile");

        for(String ID: playerIDs){
            var playerProfileList = profileDataStore.list(new ProfileQuery(Long.parseLong(ID)));

            if(!playerProfileList.isEmpty()){
                playerProfiles.add(playerProfileList.get(0));
            }
        }

        return new ProfilePayload(playerProfiles);

    }

    public GameRating rating(Session session){
        GameRating[] loaded  = {new GameRating()};
        CurrentSaveIndex currentSaveIndex = platformGameServiceProvider.savedGameServiceProvider().currentSaveIndex(session);
        RecoverableQuery<GameRating> query = RecoverableQuery.query(currentSaveIndex.saveId,loaded[0], GamePortableRegistry.INS);
        mDataStore.list(query,(m)->{
            if(m.label().equals(loaded[0].label())){
                loaded[0]=m;
                return false;
            }
            return true;
        });
        if(loaded[0].distributionId()==0){
            loaded[0].ownerKey(query.key());
            loaded[0].rank = 1;
            loaded[0].xp = 100;
            mDataStore.create(loaded[0]);
        }
        loaded[0].dataStore(mDataStore);
        return loaded[0];
    }
    public Stub stub(Session session,Descriptor lobby){
        DataStore ds = applicationPreSetup.dataStore(gameCluster,NAME+"_"+lobby.tag().replaceAll(Recoverable.PATH_SEPARATOR,"_"));
        Stub stub = new Stub();
        stub.distributionId(session.stub());
        stub.systemId(session.systemId());
        ds.createIfAbsent(stub,true);
        stub.dataStore(ds);
        return stub;
    }
    public Statistics statistics(Session session){
        CurrentSaveIndex currentSaveIndex = platformGameServiceProvider.savedGameServiceProvider().currentSaveIndex(session);
        UserStatistics deltaStatistics = new UserStatistics();
        deltaStatistics.distributionId(currentSaveIndex.saveId);
        deltaStatistics.dataStore(applicationPreSetup.dataStore(gameCluster,NAME+"_statistics"));
        deltaStatistics.load();
        deltaStatistics.registerListener((entry -> {
            LeaderBoard leaderBoard = platformLeaderBoardProvider.leaderBoard(entry.name());
            leaderBoard.onAllBoard(entry);
        }));
        return deltaStatistics;
    }

    public boolean save(Session session, MappingObject mappingObject){
        CurrentSaveIndex currentSaveIndex = platformGameServiceProvider.savedGameServiceProvider().currentSaveIndex(session);
        RecoverableQuery<MappingObject> query = RecoverableQuery.query(currentSaveIndex.saveId,mappingObject, PresencePortableRegistry.INS);
        boolean[] updated ={false};
        mDataStore.list(query,(m)->{
           if(m.label().equals(mappingObject.label())){
               m.value(mappingObject.value());
               mDataStore.update(m);
               updated[0]=true;
               return false;
           }
           return true;
        });
        if(!updated[0]){
            mappingObject.ownerKey(query.key());
            mDataStore.create(mappingObject);
        }
        return true;
    }

    public boolean load(Session session,MappingObject mappingObject){
        CurrentSaveIndex currentSaveIndex = platformGameServiceProvider.savedGameServiceProvider().currentSaveIndex(session);
        RecoverableQuery<MappingObject> query = RecoverableQuery.query(currentSaveIndex.saveId,mappingObject, PresencePortableRegistry.INS);
        boolean[] loaded ={false};
        mDataStore.list(query,(m)->{
            if(m.label().equals(mappingObject.label())){
                mappingObject.value(m.value());
                loaded[0]=true;
                return false;
            }
            return true;
        });
        return loaded[0];
    }
    public SavedGame resetSavedGame(CurrentSaveIndex currentSaveIndex){
        if(currentSaveIndex.index()==null) return null;
        SavedGame savedGame = savedGame(currentSaveIndex.index());
        savedGame.stub = 0;
        savedGame.version = 0;
        savedGame.name("New Save");
        savedGame.timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
        this.dataStore.update(savedGame);
        return  savedGame;
    }

    public void updateSavedGame(CurrentSaveIndex currentSaveIndex){
        if(currentSaveIndex.index()==null) return;
        SavedGame savedGame = savedGame(currentSaveIndex.index());
        savedGame.version = savedGame.version+1;
        savedGame.timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
        savedGame.update();
    }
    public void expireSavedGame(CurrentSaveIndex currentSaveIndex){
        if(currentSaveIndex.index()==null) return;
        SavedGame savedGame = savedGame(currentSaveIndex.index());
        savedGame.expireSession(currentSaveIndex.routingNumber());
    }

    public PersonalDataIndex loadPersonalDataIndex(String systemId){
        PersonalDataIndex playerSaveIndex = new PersonalDataIndex();
        playerSaveIndex.distributionKey(systemId);
        dataStore.createIfAbsent(playerSaveIndex,true);
        playerSaveIndex.dataStore(dataStore);
        return playerSaveIndex;
    }

    private SavedGameIndex savedGameIndex(String systemId){
        SavedGameIndex savedGameIndex = new SavedGameIndex();
        savedGameIndex.distributionKey(systemId);
        savedGameIndex.dataStore(this.dataStore);
        this.dataStore.createIfAbsent(savedGameIndex,true);
        return savedGameIndex;
    }
    private SavedGame savedGame(String saveId){
        SavedGame savedGame = new SavedGame();
        savedGame.distributionKey(saveId);
        if(!dataStore.load(savedGame)) return null;
        savedGame.dataStore(dataStore);
        return savedGame;
    }
    private void deviceIndex(String systemId,String deviceId){
        //AccessIndex accessIndex = serviceContext.clusterProvider().accessIndexService().setIfAbsent(deviceId,AccessIndex.DEVICE_INDEX);
        //DeviceSaveIndex deviceSaveIndex = new DeviceSaveIndex(accessIndex.distributionKey());
        //this.dataStore.createIfAbsent(deviceSaveIndex,true);
        //if(deviceSaveIndex.addKey(systemId)) this.dataStore.update(deviceSaveIndex);
    }

    private void syncPlayList(){
        if(updates.getAndSet(0)>0) recentlyPlayList.update();
        this.serviceContext.schedule(scheduleRunner);
    }

    public void onLeave(Session session){
        platformGameServiceProvider.savedGameServiceProvider().checkSavedGame(session.distributionKey());
    }

    public void onLobby(Descriptor onLobby){
        applicationPreSetup.dataStore(gameCluster,NAME+"_"+onLobby.tag().replaceAll(Recoverable.PATH_SEPARATOR,"_"));
    }

}
