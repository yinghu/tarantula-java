package com.tarantula.platform.presence;


import com.icodesoftware.*;
import com.icodesoftware.service.ServiceContext;

import com.icodesoftware.util.TimeUtil;
import com.tarantula.game.Rating;

import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.game.service.PlatformGameServiceSetup;
import com.tarantula.platform.leaderboard.PlatformLeaderBoardProvider;

import com.tarantula.platform.presence.saves.*;

import com.tarantula.platform.statistics.UserStatistics;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PlatformPresenceServiceProvider extends PlatformGameServiceSetup {

    public static final String NAME = "presence";

    private int recentlyPlayListSize;
    private int friendListSize;
    private PlayList recentlyPlayList;
    private PlatformLeaderBoardProvider platformLeaderBoardProvider;

    public PlatformPresenceServiceProvider(PlatformGameServiceProvider gameServiceProvider){
        super(gameServiceProvider,NAME);
    }


    @Override
    public void start() throws Exception {
        this.recentlyPlayList = new PlayList(recentlyPlayListSize);
        this.recentlyPlayList.distributionKey(this.gameCluster.distributionKey());
        this.dataStore.createIfAbsent(this.recentlyPlayList,true);
        this.recentlyPlayList.dataStore(this.dataStore);
        logger.warn("Presence service provider started->"+gameServiceName);
    }

    @Override
    public void waitForData(){
        this.platformLeaderBoardProvider = platformGameServiceProvider.leaderBoardProvider();
        Configuration configuration = serviceContext.configuration("game-presence-settings");
        this.recentlyPlayListSize = ((Number)configuration.property("recentlyPlayListSize")).intValue();
        this.friendListSize = ((Number)configuration.property("friendListSize")).intValue();
    }
    @Override
    public void setup(ServiceContext serviceContext) {
        super.setup(serviceContext);
        this.dataStore = this.applicationPreSetup.dataStore(gameCluster,NAME);
        this.logger = serviceContext.logger(PlatformPresenceServiceProvider.class);
        this.logger.warn("Presence service provider started on ->"+gameServiceName);
    }
    public void onFriendList(String systemId,String friendSystemId){
        PlayList playList = new PlayList(friendListSize);
        playList.distributionKey(systemId);
        this.dataStore.createIfAbsent(playList,true);
        playList.playListIndex.push(friendSystemId);
        this.dataStore.update(playList);
    }
    public void onPlay(String systemId){//blocked
        this.recentlyPlayList.playListIndex.push(systemId);
        this.recentlyPlayList.update();
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
        profile.iconUrl = "resource/portrait.png";
        profile.distributionKey(systemId);
        this.dataStore.createIfAbsent(profile,true);
        profile.dataStore(this.dataStore);
        return profile;
    }
    public Rating rating(Session session){
        Rating rating = new Rating();
        this.platformGameServiceProvider.savedGameServiceProvider().createIfAbsent(session,rating);
        if(rating.granted) return rating;
        rating.granted = this.platformGameServiceProvider.resourceServiceProvider().initializeInventory(session.systemId());
        rating.update();
        return rating;
    }
    public Statistics statistics(Session session){
        UserStatistics deltaStatistics = new UserStatistics();
        this.platformGameServiceProvider.savedGameServiceProvider().createIfAbsent(session,deltaStatistics);
        deltaStatistics.registerListener((entry -> {
            LeaderBoard leaderBoard = platformLeaderBoardProvider.leaderBoard(entry.name());
            leaderBoard.onAllBoard(entry);
        }));
        return deltaStatistics;
    }

    public List<SavedGame> listSaves(String systemId,String deviceId){
        platformGameServiceProvider.savedGameServiceProvider().checkSavedGame(systemId);
        deviceIndex(systemId,deviceId);
        SavedGameIndex savedGameIndex = savedGameIndex(systemId);
        return savedGameIndex.list(platformGameServiceProvider.savedGameServiceProvider().saveSize());
    }

    public CurrentSaveIndex selectSave(Session session, String saveId){
        SavedGameIndex savedGameIndex = savedGameIndex(session.systemId());
        SavedGame selected = savedGameIndex.select(saveId);
        if(!selected.onSession(session)) return null;
        return this.platformGameServiceProvider.savedGameServiceProvider().selectSavedGame(session,selected,currentSaveIndex -> {
            if(currentSaveIndex.index()==null) return;
            SavedGame released = savedGame(currentSaveIndex.index());
            released.offSession(session);
        });
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
        AccessIndex accessIndex = serviceContext.accessIndexService().setIfAbsent(deviceId,AccessIndex.DEVICE_INDEX);
        DeviceSaveIndex deviceSaveIndex = new DeviceSaveIndex(accessIndex.distributionKey());
        this.dataStore.createIfAbsent(deviceSaveIndex,true);
        if(deviceSaveIndex.addKey(systemId)) this.dataStore.update(deviceSaveIndex);
    }

    public void onLeave(Session session){
        platformGameServiceProvider.savedGameServiceProvider().checkSavedGame(session.systemId());
    }

}
