package com.tarantula.platform.presence.dailygiveaway;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;
import com.icodesoftware.Configuration;
import com.icodesoftware.Descriptor;
import com.icodesoftware.Session;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.inventory.PlatformInventoryServiceProvider;
import com.tarantula.platform.item.PlatformItemServiceProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PlatformDailyGiveawayServiceProvider extends PlatformItemServiceProvider {

    public static final String NAME = "giveaway";

    private final PlatformInventoryServiceProvider inventoryServiceProvider;

    private int dailyLoginPendingHours;
    private int maxConsecutiveDays;
    private int maxRewardTier;
    private ConcurrentHashMap<String,DailyGiveaway> dailyGiveaways;
    public PlatformDailyGiveawayServiceProvider(PlatformGameServiceProvider gameServiceProvider){
        super(gameServiceProvider,NAME);
        this.inventoryServiceProvider = gameServiceProvider.inventoryServiceProvider();
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
        this.logger = serviceContext.logger(PlatformDailyGiveawayServiceProvider.class);
        this.logger.warn("Daily giveaway service provider started on ->"+gameServiceName);
    }

    public DailyLoginTrack checkDailyLogin(Session session){
        DailyLoginTrack dailyLoginTrack = new DailyLoginTrack();
        platformGameServiceProvider.savedGameServiceProvider().createIfAbsent(session,dailyLoginTrack);
        if(dailyLoginTrack.rewardPending) return dailyLoginTrack;
        boolean rewarded = dailyLoginTrack.checkDailyLogin(dailyLoginPendingHours,maxConsecutiveDays,maxRewardTier);
        return rewarded?dailyLoginTrack:null;
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
        List<DailyGiveaway> items = applicationPreSetup.list(descriptor,new DailygGiveawayObjectQuery("category/DailyGiveaway"));
        items.forEach((a)-> {
            if(!a.disabled()) {
                a.setup();
                dailyGiveaways.put(a.name(),a);
            }
        });
        return null;
    }

    public boolean redeem(String systemId){
        DailyLoginTrack dailyLoginTrack = new DailyLoginTrack();
        //dailyLoginTrack.distributionKey(gameId);
        //dailyLoginTrack.dataStore(dataStore);
        //if(!this.dataStore.load(dailyLoginTrack)) return false;
        if(!dailyLoginTrack.rewardPending || !dailyGiveaways.containsKey(dailyLoginTrack.rewardKey())) return false;
        dailyLoginTrack.rewardPending = !this.inventoryServiceProvider.redeem(systemId,dailyGiveaways.get(dailyLoginTrack.rewardKey()));
        dailyLoginTrack.update();
        return !dailyLoginTrack.rewardPending;
    }
}
