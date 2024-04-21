package com.tarantula.platform.presence.leaderboard;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Configuration;
import com.icodesoftware.DataStore;
import com.icodesoftware.LeaderBoard;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.game.service.PlatformGameServiceSetup;
import com.tarantula.platform.event.ServerPushEvent;

import java.util.concurrent.ConcurrentHashMap;

public class PlatformLeaderBoardProvider extends PlatformGameServiceSetup implements LeaderBoard.Listener {


    public static final String NAME = "leaderboard";

    private TarantulaLogger logger;
    private String topic;
    private int leaderBoardSize = 10;
    private DataStore dataStore;

    private ConcurrentHashMap<String, LeaderBoardSync> tMap = new ConcurrentHashMap<>();

    public PlatformLeaderBoardProvider(PlatformGameServiceProvider gameServiceProvider){
        super(gameServiceProvider,NAME);
    }
    public LeaderBoardSync leaderBoard(String category){
        return tMap.computeIfAbsent(category,(s)->{
            LeaderBoardSync ldb = new LeaderBoardSync(category,leaderBoardSize,gameCluster.distributionId(),this);
            ldb.dataStore(this.dataStore);
            ldb.load();
            return ldb;
        });
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        Configuration configuration = serviceContext.configuration("game-presence-settings");
        JsonObject plist = ((JsonElement)configuration.property("leaderBoard")).getAsJsonObject();
        this.leaderBoardSize = plist.get("topListSize").getAsInt();
        this.dataStore = gameCluster.applicationPreSetup().dataStore(gameCluster,NAME);//typeId_service

        this.topic = this.platformGameServiceProvider.registerEventListener(NAME,(e)->{
            if(e instanceof ServerPushEvent){
                //LeaderBoard.Entry update = this.gsonBuilder.create().fromJson(new String(e.payload()),LeaderBoardEntry.class);
                //LeaderBoardSync ldb = this.leaderBoard(update.category());
                //ldb.onView(update);
            }
            return false;
        });
        this.logger = JDKLogger.getLogger(PlatformLeaderBoardProvider.class);
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void start() throws Exception {
        logger.warn("Leader board service provider started on ["+gameCluster.serviceType()+"] with topic ["+topic+"]["+leaderBoardSize+"]");
    }

    @Override
    public void shutdown() throws Exception {

    }

    @Override
    public void onUpdated(LeaderBoard.Entry entry) {
        //byte[] payload = this.gsonBuilder.create().toJson(entry).getBytes();
        //this.serviceContext.postOffice().onTopic(topic).send(NAME,payload);
    }
    @Override
    public void atMidnight(){
        tMap.forEach((k,v)->{
            v.reset();
        });
    }

}
