package com.tarantula.platform.leaderboard;

import com.google.gson.GsonBuilder;
import com.icodesoftware.DataStore;
import com.icodesoftware.LeaderBoard;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.ServiceProvider;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.event.ServerPushEvent;

import java.util.concurrent.ConcurrentHashMap;

public class PlatformLeaderBoardProvider implements ServiceProvider, LeaderBoard.Listener {


    public static final String NAME = "leaderboard";

    private TarantulaLogger logger;
    private String topic;
    private static int LDB_SIZE = 10;
    private DataStore dataStore;

    private GameCluster gameCluster;

    private ServiceContext serviceContext;
    private GsonBuilder gsonBuilder;

    private final PlatformGameServiceProvider gameServiceProvider;
    private ConcurrentHashMap<String, LeaderBoardSync> tMap = new ConcurrentHashMap<>();

    public PlatformLeaderBoardProvider(PlatformGameServiceProvider gameServiceProvider){
        this.gameServiceProvider = gameServiceProvider;
        this.gameCluster = gameServiceProvider.gameCluster();
    }
    public LeaderBoardSync leaderBoard(String category){
        return tMap.computeIfAbsent(category,(s)->{
            LeaderBoardSync ldb = new LeaderBoardSync(category,LDB_SIZE);
            ldb.dataStore(this.dataStore);
            ldb.masterListener(this);
            ldb.load();
            return ldb;
        });
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        this.dataStore = gameCluster.applicationPreSetup().dataStore(gameCluster,NAME);//typeId_service
        this.gsonBuilder = new GsonBuilder();
        this.gsonBuilder.registerTypeAdapter(LeaderBoardEntry.class,new LeaderBoardEntrySerializer());
        this.gsonBuilder.registerTypeAdapter(LeaderBoardEntry.class,new LeaderBoardEntryDeserializer());
        this.topic = this.gameServiceProvider.registerEventListener(NAME,(e)->{
            if(e instanceof ServerPushEvent){
                LeaderBoard.Entry update = this.gsonBuilder.create().fromJson(new String(e.payload()),LeaderBoardEntry.class);
                LeaderBoardSync ldb = this.leaderBoard(update.category());
                ldb.onView(update);
            }
            return false;
        });
        this.logger = serviceContext.logger(PlatformLeaderBoardProvider.class);
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void start() throws Exception {
        logger.warn("Leader board service provider started on ["+gameCluster.serviceType()+"] with topic ["+topic+"]");
    }

    @Override
    public void shutdown() throws Exception {

    }

    @Override
    public void onUpdated(LeaderBoard.Entry entry) {
        byte[] payload = this.gsonBuilder.create().toJson(entry).getBytes();
        this.serviceContext.postOffice().onTopic(topic).send(NAME,payload);
    }
    @Override
    public void atMidnight(){
        tMap.forEach((k,v)->{
            v.reset();
        });
    }

}
