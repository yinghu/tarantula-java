package com.tarantula.platform.presence;


import com.icodesoftware.*;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.ServiceProvider;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.game.Rating;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.leaderboard.PlatformLeaderBoardProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.presence.saves.*;
import com.tarantula.platform.service.ApplicationPreSetup;
import com.tarantula.platform.statistics.UserStatistics;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PlatformPresenceServiceProvider implements ServiceProvider {

    public static final String NAME = "presence";

    private TarantulaLogger logger;
    private final String gameServiceName;
    private final GameCluster gameCluster;
    private ServiceContext serviceContext;
    private DataStore presenceDataStore;
    private ApplicationPreSetup applicationPreSetup;


    private int recentlyPlayListSize;
    private int friendListSize;

    private int saveSize;

    private PlayList recentlyPlayList;
    private PlatformLeaderBoardProvider platformLeaderBoardProvider;
    private PlatformGameServiceProvider gameServiceProvider;

    public PlatformPresenceServiceProvider(PlatformGameServiceProvider gameServiceProvider){
        this.gameServiceProvider = gameServiceProvider;
        this.gameCluster = gameServiceProvider.gameCluster();
        this.gameServiceName = gameCluster.serviceType();
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void start() throws Exception {
        this.recentlyPlayList = new PlayList(recentlyPlayListSize);
        this.recentlyPlayList.distributionKey(this.gameCluster.distributionKey());
        this.presenceDataStore.createIfAbsent(this.recentlyPlayList,true);
        this.recentlyPlayList.dataStore(this.presenceDataStore);
        logger.warn("Presence service provider started->"+gameServiceName);
    }

    @Override
    public void shutdown() throws Exception {

    }
    @Override
    public void waitForData(){
        this.platformLeaderBoardProvider = gameServiceProvider.leaderBoardProvider();
        Configuration configuration = serviceContext.configuration("game-presence-settings");
        this.recentlyPlayListSize = ((Number)configuration.property("recentlyPlayListSize")).intValue();
        this.friendListSize = ((Number)configuration.property("friendListSize")).intValue();
    }
    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        this.applicationPreSetup = gameCluster.applicationPreSetup();
        this.presenceDataStore = this.applicationPreSetup.dataStore(gameCluster,NAME);
        this.logger = serviceContext.logger(PlatformPresenceServiceProvider.class);
        this.logger.warn("Presence service provider started on ->"+gameServiceName);
    }
    public void onFriendList(String systemId,String friendSystemId){
        PlayList playList = new PlayList(friendListSize);
        playList.distributionKey(systemId);
        this.presenceDataStore.createIfAbsent(playList,true);
        playList.playListIndex.push(friendSystemId);
        this.presenceDataStore.update(playList);
    }
    public void onPlay(String systemId){//blocked
        this.recentlyPlayList.playListIndex.push(systemId);
        this.recentlyPlayList.update();
    }
    public List<String> friendList(String systemId){
        PlayList playList = new PlayList(friendListSize);
        playList.distributionKey(systemId);
        this.presenceDataStore.createIfAbsent(playList,true);
        return playList.playListIndex.list(new ArrayList<>());
    }
    public List<String> recentlyPlayList(){
        return this.recentlyPlayList.playListIndex.list(new ArrayList<>());
    }

    public Profile profile(String systemId){
        Profile profile = new Profile();
        profile.displayName ="player";
        profile.iconUrl = "resource/portrait.png";
        profile.distributionKey(systemId);
        this.presenceDataStore.createIfAbsent(profile,true);
        profile.dataStore(this.presenceDataStore);
        return profile;
    }
    public Rating rating(String systemId){
        Rating rating = new Rating();
        rating.distributionKey(systemId);
        this.presenceDataStore.createIfAbsent(rating,true);
        rating.dataStore(this.presenceDataStore);
        if(rating.granted) return rating;
        rating.granted = this.gameServiceProvider.resourceServiceProvider().initializeInventory(systemId);
        rating.update();
        return rating;
    }
    public Statistics statistics(String systemId){
        UserStatistics deltaStatistics = new UserStatistics();
        deltaStatistics.distributionKey(systemId);
        deltaStatistics.dataStore(this.presenceDataStore);
        this.presenceDataStore.createIfAbsent(deltaStatistics,true);
        deltaStatistics.registerListener((entry -> {
            LeaderBoard leaderBoard = platformLeaderBoardProvider.leaderBoard(entry.name());
            leaderBoard.onAllBoard(entry);
        }));
        return deltaStatistics;
    }

    public List<SavedGame> listSaves(String systemId,String deviceId){
        deviceIndex(systemId,deviceId);
        SavedGameIndex savedGameIndex = savedGameIndex(systemId);
        return savedGameIndex.list(gameServiceProvider.savedGameServiceProvider().saveSize());
    }

    public CurrentSaveIndex selectSave(Session session, String saveId){
        SavedGameIndex savedGameIndex = savedGameIndex(session.systemId());
        SavedGame selected = savedGameIndex.select(saveId);
        if(!selected.onSession(session)) return null;
        return this.gameServiceProvider.savedGameServiceProvider().selectSavedGame(session,selected,currentSaveIndex -> {
            SavedGame released = savedGame(currentSaveIndex.index());
            released.offSession(session);
            return false;
        });
    }
    public SavedGame resetSavedGame(CurrentSaveIndex currentSaveIndex){
        if(currentSaveIndex.index()==null) return null;
        SavedGame savedGame = savedGame(currentSaveIndex.index());
        savedGame.stub = 0;
        savedGame.version = 0;
        savedGame.name("New Save");
        savedGame.timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
        this.presenceDataStore.update(savedGame);
        return  savedGame;
    }

    public void updateSavedGame(CurrentSaveIndex currentSaveIndex){
        if(currentSaveIndex.index()==null) return;
        SavedGame savedGame = savedGame(currentSaveIndex.index());
        savedGame.version = savedGame.version+1;
        savedGame.timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
        savedGame.update();
    }

    public PersonalDataIndex loadPersonalDataIndex(String systemId){
        PersonalDataIndex playerSaveIndex = new PersonalDataIndex();
        playerSaveIndex.distributionKey(systemId);
        presenceDataStore.createIfAbsent(playerSaveIndex,true);
        playerSaveIndex.dataStore(presenceDataStore);
        return playerSaveIndex;
    }

    private SavedGameIndex savedGameIndex(String systemId){
        SavedGameIndex savedGameIndex = new SavedGameIndex();
        savedGameIndex.distributionKey(systemId);
        savedGameIndex.dataStore(this.presenceDataStore);
        this.presenceDataStore.createIfAbsent(savedGameIndex,true);
        return savedGameIndex;
    }
    private SavedGame savedGame(String saveId){
        SavedGame savedGame = new SavedGame();
        savedGame.distributionKey(saveId);
        if(!presenceDataStore.load(savedGame)) return null;
        savedGame.dataStore(presenceDataStore);
        return savedGame;
    }
    private void deviceIndex(String systemId,String deviceId){
        AccessIndex accessIndex = serviceContext.accessIndexService().setIfAbsent(deviceId,AccessIndex.DEVICE_INDEX);
        DeviceSaveIndex deviceSaveIndex = new DeviceSaveIndex(accessIndex.distributionKey());
        this.presenceDataStore.createIfAbsent(deviceSaveIndex,true);
        if(deviceSaveIndex.addKey(systemId)) this.presenceDataStore.update(deviceSaveIndex);
    }
    public void onJoin(Session session){
        logger.warn("Join->"+session.systemId()+">>"+session.stub());
    }
    public void onLeave(Session session){
        gameServiceProvider.savedGameServiceProvider().selectSavedGame(session,currentSaveIndex -> {
            if(currentSaveIndex.index()==null) return false;
            SavedGame savedGame = savedGame(currentSaveIndex.index());
            savedGame.offSession(session);
            return true;
        });
    }
}
