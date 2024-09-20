package com.tarantula.platform.presence.dailygiveaway;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;
import com.icodesoftware.Configuration;
import com.icodesoftware.Descriptor;
import com.icodesoftware.Session;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.item.PlatformItemServiceProvider;
import com.tarantula.platform.presence.saves.CurrentSaveIndex;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PlatformDailyGiveawayServiceProvider extends PlatformItemServiceProvider {

    public static final String NAME = "dailylogin";

    private int dailyLoginPendingHours;
    private int maxConsecutiveDays;
    private int maxRewardTier;
    private ConcurrentHashMap<String,DailyGiveaway> dailyGiveaways;
    public PlatformDailyGiveawayServiceProvider(PlatformGameServiceProvider gameServiceProvider){
        super(gameServiceProvider,NAME);
        this.dailyGiveaways = new ConcurrentHashMap<>();
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        super.setup(serviceContext);
        Configuration configuration = serviceContext.configuration("game-presence-settings");
        JsonObject dailyReward = ((JsonElement)configuration.property("dailyReward")).getAsJsonObject();
        dailyLoginPendingHours = dailyReward.get("waitingTimeHours").getAsInt();
        maxConsecutiveDays = dailyReward.get("maxConsecutiveDays").getAsInt();
        maxRewardTier = dailyReward.get("maxRewardTiers").getAsInt();
        this.dataStore = applicationPreSetup.dataStore(gameCluster,NAME);
        this.logger = JDKLogger.getLogger(PlatformDailyGiveawayServiceProvider.class);
        this.logger.warn("Daily giveaway service provider started on ->"+gameServiceName);
    }

    @Override
    public void start() throws Exception{
        if(serviceContext.node().homingAgent().enabled()){
            String config = serviceContext.node().homingAgent().onConfiguration(gameCluster.distributionId(),"DailyReward");
            logger.warn(config);
        }
    }

    public DailyLoginTrack claim(Session session){
        CurrentSaveIndex currentSaveIndex = platformGameServiceProvider.savedGameServiceProvider().currentSaveIndex(session);
        DailyLoginTrack dailyLoginTrack = DailyLoginTrack.lookup(currentSaveIndex.saveId,dataStore);
        if(dailyLoginTrack.rewardPending) return dailyLoginTrack;
        boolean rewarded = dailyLoginTrack.checkDailyLogin(dailyLoginPendingHours,maxConsecutiveDays,maxRewardTier);
        if(!rewarded) return null;
        DailyGiveaway dailyGiveaway = dailyGiveaways.get(dailyLoginTrack.rewardKey());
        if(dailyGiveaway==null) return null;
        platformGameServiceProvider.inventoryServiceProvider().redeem(session.systemId(),dailyGiveaway);
        dailyLoginTrack.rewardPending = false;
        dailyLoginTrack.update();
        return dailyLoginTrack;
    }
    public List<DailyGiveaway> list(){
        ArrayList<DailyGiveaway> _items = new ArrayList<>();
        dailyGiveaways.forEach((k,v)-> _items.add(v));
        return _items;
    }

    @Override
    public boolean onItemRegistered(String category, String itemId) {
        DailyGiveaway dailyGiveaway = new DailyGiveaway();
        dailyGiveaway.distributionKey(itemId);
        Descriptor app = gameCluster.serviceWithCategory(category);
        if(!applicationPreSetup.load(app,dailyGiveaway)){
            return false;
        }
        dailyGiveaway.setup();
        dailyGiveaways.put(dailyGiveaway.name(),dailyGiveaway);
        return true;
    }
    public boolean onItemReleased(String category,String itemId){
        String[] released ={null};
        dailyGiveaways.forEach((k,v)->{
            if(v.distributionKey().equals(itemId)) released[0]=k;
        });
        if(released[0]!=null) dailyGiveaways.remove(released[0]);
        return false;
    }
    @Override
    public String registerConfigurableListener(Descriptor descriptor, Configurable.Listener listener) {
        List<DailyGiveaway> items = applicationPreSetup.list(descriptor,new DailyGiveawayObjectQuery(descriptor.key()));
        items.forEach((a)-> {
            logger.warn("<><><>"+a.name()+"<><>"+a.day());
            if(!a.disabled()) {
                a.configurableSetting(gameCluster.configurableCategories(Configurable.APPLICATION_CONFIG_TYPE));
                a.setup();
                dailyGiveaways.put(a.name(),a);
            }
        });
        return null;
    }

    public boolean onItemRegistered(int publishId){
        String config = serviceContext.node().homingAgent().onConfigurationRegistered(publishId);
        logger.warn(config);
        return true;
    }
    public boolean onItemReleased(int publishId){
        logger.warn("release local resource with ["+publishId+"]");
        return true;
    }


}
