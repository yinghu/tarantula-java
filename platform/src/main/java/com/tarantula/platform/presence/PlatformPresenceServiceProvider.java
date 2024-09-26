package com.tarantula.platform.presence;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.protocol.statistics.UserRating;
import com.icodesoftware.protocol.statistics.UserStatistics;
import com.icodesoftware.service.ServiceContext;

import com.icodesoftware.util.ScheduleRunner;
import com.icodesoftware.util.SnowflakeKey;
import com.icodesoftware.util.TimeUtil;


import com.tarantula.game.Stub;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.game.service.PlatformGameServiceSetup;

import com.tarantula.platform.presence.leaderboard.PlatformLeaderBoardProvider;

import com.tarantula.platform.presence.saves.*;

import com.tarantula.platform.util.RecoverableQuery;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
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
    private DataStore profileDataStore;
    private DataStore playListDataStore;

    private DistributionPresenceService distributionPresenceService;
    private ConcurrentHashMap<String,ProfileNameSequence> profileNameSequenceMapping;

    public PlatformPresenceServiceProvider(PlatformGameServiceProvider gameServiceProvider){
        super(gameServiceProvider,NAME);
        updates = new AtomicInteger(0);
        profileNameSequenceMapping = new ConcurrentHashMap<>();
    }


    @Override
    public void start() throws Exception {
        this.profileDataStore.list(new ProfileNameSequenceQuery(gameCluster.distributionId()),(profileNameSequence -> {
            profileNameSequence.dataStore(profileDataStore);
            profileNameSequenceMapping.put(profileNameSequence.name(),profileNameSequence);
            return true;
        }));
        profileNameSequenceMapping.forEach((n,p)->{
            if(p.distributionId()==0){
                p.ownerKey(SnowflakeKey.from(gameCluster.distributionId()));
                if(this.profileDataStore.create(p)) p.dataStore(profileDataStore);
            }
        });
        this.recentlyPlayList = new PlayList(recentlyPlayListSize);
        this.recentlyPlayList.distributionId(this.gameCluster.distributionId());
        this.playListDataStore.createIfAbsent(this.recentlyPlayList,true);
        this.recentlyPlayList.dataStore(this.playListDataStore);
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
        JsonArray objsPreloaded = ((JsonElement)configuration.property("profile")).getAsJsonObject().get("adjectives").getAsJsonArray();
        JsonArray namesPreloaded = ((JsonElement)configuration.property("profile")).getAsJsonObject().get("nouns").getAsJsonArray();
        objsPreloaded.forEach(obj->{
            String adjective = obj.getAsString();
            namesPreloaded.forEach(pn->{
                String pname = adjective+pn.getAsString();
                profileNameSequenceMapping.put(pname,new ProfileNameSequence(pname));
            });
        });
        JsonObject plist = ((JsonElement)configuration.property("playList")).getAsJsonObject();
        this.recentlyPlayListSize = plist.get("recentlyListSize").getAsInt();
        this.friendListSize = plist.get("friendListSize").getAsInt();
        this.syncIntervalSeconds = plist.get("syncIntervalSeconds").getAsLong();
        this.dataStore = this.applicationPreSetup.dataStore(gameCluster,NAME);
        this.mDataStore = this.applicationPreSetup.dataStore(gameCluster,NAME+"_mapping_object");
        this.profileDataStore = this.applicationPreSetup.dataStore(gameCluster,NAME+"_profile");
        this.playListDataStore = this.applicationPreSetup.dataStore(gameCluster,NAME+"_play_list");
        this.distributionPresenceService = this.serviceContext.clusterProvider().serviceProvider(DistributionPresenceService.NAME);
        this.logger = JDKLogger.getLogger(PlatformPresenceServiceProvider.class);
        this.logger.warn("Presence service provider started on ->"+gameServiceName);
    }


    public void onPlay(Session session){
        this.recentlyPlayList.onList(session.distributionId());
        updates.incrementAndGet();
    }


    public void onFriendList(long systemId,long friendSystemId){
        PlayList playList = new PlayList(friendListSize);
        playList.distributionId(systemId);
        this.playListDataStore.createIfAbsent(playList,true);
        playList.onList(friendSystemId);
        this.playListDataStore.update(playList);
    }
    public void onPlay(long systemId){
        this.recentlyPlayList.onList(systemId);
        updates.incrementAndGet();
    }
    public List<Long> friendList(long systemId){
        PlayList playList = new PlayList(friendListSize);
        playList.distributionId(systemId);
        this.playListDataStore.createIfAbsent(playList,true);
        return playList.list();
    }
    public List<Long> recentlyPlayList(){
        return this.recentlyPlayList.list();
    }

    public Profile profile(String systemId){
        Profile profile = new Profile();
        //profile.displayName ="player";
        //profile.distributionKey(systemId);
        //this.dataStore.createIfAbsent(profile,true);
        //profile.dataStore(this.dataStore);
        return profile;
    }

    public boolean createProfile(Session session){

        DataStore profileDataStore = applicationPreSetup.onDataStore("profile");

        Profile profile = new Profile();
        profile.distributionId(session.distributionId());

        profileDataStore.createIfAbsent(profile, true);
        if(!profile.configureAndValidate(session.payload())) return false;
        profile.profileSequence = distributionPresenceService.profileSequence(gameCluster.serviceType(),profile.displayName);
        return profileDataStore.update(profile);
    }

    public ProfilePayload getProfilePayload(String IDs){
        String[] playerIDs = IDs.split("#");

        List<Profile> playerProfiles = new ArrayList<>();
        DataStore profileDataStore = applicationPreSetup.onDataStore("profile");

        for(String ID: playerIDs){
            Profile profileLoaded = new Profile();
            profileLoaded.distributionId(Long.parseLong(ID));
            if(!profileDataStore.load(profileLoaded)) continue;

            playerProfiles.add(profileLoaded);
        }

        return new ProfilePayload(playerProfiles);
    }

    public Rating rating(Session session){
        UserRating rating  = new UserRating();
        CurrentSaveIndex currentSaveIndex = platformGameServiceProvider.savedGameServiceProvider().currentSaveIndex(session);
        rating.distributionId(currentSaveIndex.saveId);
        rating.dataStore(applicationPreSetup.dataStore(gameCluster,NAME+"_rating"));
        rating.load();
        return rating;
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
        deltaStatistics.registerListener(((entry,delta) -> {
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


    public PersonalDataIndex loadPersonalDataIndex(String systemId){
        PersonalDataIndex playerSaveIndex = new PersonalDataIndex();
        playerSaveIndex.distributionKey(systemId);
        dataStore.createIfAbsent(playerSaveIndex,true);
        playerSaveIndex.dataStore(dataStore);
        return playerSaveIndex;
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
        //platformGameServiceProvider.savedGameServiceProvider().checkSavedGame(session.distributionKey());
    }

    public void onLobby(Descriptor onLobby){
        applicationPreSetup.dataStore(gameCluster,NAME+"_"+onLobby.tag().replaceAll(Recoverable.PATH_SEPARATOR,"_"));
    }


    public int onProfileSequence(String name){
        ProfileNameSequence profileNameSequence = profileNameSequenceMapping.computeIfAbsent(name,(k)->{
            //Just in case the name is not preloaded
            ProfileNameSequence notCached = new ProfileNameSequence(name);
            notCached.ownerKey(SnowflakeKey.from(gameCluster.distributionId()));
            notCached.dataStore(dataStore);
            dataStore.create(notCached);
            return notCached;
        });
        return profileNameSequence.sequence();
    }

}
