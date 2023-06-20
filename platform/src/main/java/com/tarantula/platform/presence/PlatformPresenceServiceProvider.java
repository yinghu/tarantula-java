package com.tarantula.platform.presence;


import com.icodesoftware.*;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.ServiceProvider;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.game.Rating;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.leaderboard.PlatformLeaderBoardProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.presence.saves.CurrentSaveIndex;
import com.tarantula.platform.presence.saves.PlayerSaveIndex;
import com.tarantula.platform.presence.saves.SavedGame;
import com.tarantula.platform.presence.saves.SavedGameIndex;
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
        SavedGameIndex savedGameIndex = new SavedGameIndex();
        savedGameIndex.distributionKey(systemId);
        savedGameIndex.dataStore(this.presenceDataStore);
        this.presenceDataStore.createIfAbsent(savedGameIndex,true);
        return savedGameIndex.list(deviceId,save->{});
    }

    public CurrentSaveIndex selectSave(Session session, String saveId, String deviceId, String deviceName){
        SavedGame[] save = {savedGame(saveId),null};
        if(save[0]==null) throw new IllegalArgumentException("no such save with ["+saveId+"]");
        if(save[0].onDevice(session.systemId(),deviceId)){
            //set current save on save service
            save[0].name(deviceName);
            save[0].version++;
            save[0].timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
            save[0].update();
            return this.gameServiceProvider.savedGameServiceProvider().selectSavedGame(session,save[0]);
        }
        SavedGameIndex savedGameIndex = new SavedGameIndex();
        savedGameIndex.distributionKey(session.systemId());
        savedGameIndex.dataStore(this.presenceDataStore);
        this.presenceDataStore.createIfAbsent(savedGameIndex,true);
        savedGameIndex.list(deviceId,saved->{
            save[1]=saved;
        });
        //merge save[0] into save[1]
        save[1].name(deviceName);
        save[1].version = save[0].version;
        save[1].timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
        save[1].update();
        return this.gameServiceProvider.savedGameServiceProvider().selectSavedGame(session,save[1]);
    }
    public SavedGame loadSavedGame(String systemId,String gameId){
        SavedGame savedGame = new SavedGame();
        savedGame.distributionKey(gameId);
        if(!this.presenceDataStore.load(savedGame) || !savedGame.owner().equals(systemId)) return null;
        savedGame.dataStore(this.presenceDataStore);
        return  savedGame;
    }

    public PersonalDataIndex loadPersonalDataIndex(String systemId){
        PersonalDataIndex playerSaveIndex = new PersonalDataIndex();
        playerSaveIndex.distributionKey(systemId);
        presenceDataStore.createIfAbsent(playerSaveIndex,true);
        playerSaveIndex.dataStore(presenceDataStore);
        return playerSaveIndex;
    }
    private SavedGame savedGame(String saveId){
        SavedGame savedGame = new SavedGame();
        savedGame.distributionKey(saveId);
        if(!presenceDataStore.load(savedGame)) return null;
        savedGame.dataStore(presenceDataStore);
        return savedGame;
    }
}
