package com.tarantula.platform.inbox;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.RecoverService;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.SnowflakeKey;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.game.service.PlatformGameServiceSetup;
import com.tarantula.platform.PresenceIndex;
import com.tarantula.platform.inventory.PlatformInventoryServiceProvider;
import com.tarantula.platform.item.Application;

import java.util.ArrayList;
import java.util.List;

public class PlatformInboxServiceProvider extends PlatformGameServiceSetup {

    public static final String NAME = "inbox";

    @Override
    public void registerSummary(Summary summary) {
        super.registerSummary(summary);
    }

    private final PlatformInventoryServiceProvider inventoryServiceProvider;

    private boolean pendingReward;
    public PlatformInboxServiceProvider(PlatformGameServiceProvider gameServiceProvider){
        super(gameServiceProvider,NAME);
        this.inventoryServiceProvider = gameServiceProvider.inventoryServiceProvider();
    }


    public Inbox inbox(Session session){
        long systemId = session.distributionId();
        Inbox inbox = new Inbox();
        inbox.shop = this.platformGameServiceProvider.storeServiceProvider().shop("Tami");
        inbox.inventoryList = this.applicationPreSetup.inventoryList(systemId);
        inbox.rewardList = this.rewardList(systemId);
        inbox.achievementList = this.platformGameServiceProvider.achievementServiceProvider().list();
        inbox.dailyGiveawayList = this.platformGameServiceProvider.dailyGiveawayServiceProvider().list();
        inbox.accessList = this.platformGameServiceProvider.gameServiceProvider().inbox(session);
        return inbox;
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        super.setup(serviceContext);
        this.dataStore = applicationPreSetup.dataStore(gameCluster,NAME);
        Configuration configuration = serviceContext.configuration("game-presence-settings");
        JsonObject inbox = ((JsonElement)configuration.property("inbox")).getAsJsonObject();
        pendingReward = inbox.get("pendingReward").getAsBoolean();
        this.logger = JDKLogger.getLogger(PlatformInboxServiceProvider.class);
        logger.warn("Platform inbox started->"+gameServiceName);
    }

    public <T extends Application> void claim(String systemId,T item){
        if(pendingReward){
            PendingRewardIndex pendingRewardIndex = new PendingRewardIndex();
            pendingRewardIndex.distributionKey(systemId);
            this.dataStore.createIfAbsent(pendingRewardIndex,true);
            PendingReward pending = new PendingReward(item);
            this.dataStore.create(pending);
            //pendingRewardIndex.addKey(pending.distributionKey());
            this.dataStore.update(pendingRewardIndex);
            return;
        }
        this.inventoryServiceProvider.redeem(systemId,item);
    }

    public boolean redeem(Session session,String rewardKey){
        PendingReward reward = new PendingReward();
        reward.distributionKey(rewardKey);
        if(!this.dataStore.load(reward) || reward.disabled()) return false;
        if(!inventoryServiceProvider.redeem(session.systemId(),reward.toApplication())) return false;
        reward.disabled(true);
        dataStore.update(reward);
        PendingRewardIndex pendingRewardIndex = new PendingRewardIndex();
        pendingRewardIndex.distributionKey(session.systemId());
        this.dataStore.createIfAbsent(pendingRewardIndex,true);
        //pendingRewardIndex.removeKey(rewardKey);
        dataStore.update(pendingRewardIndex);
        return true;
    }
    private List<PendingReward> rewardList(long systemId){
        ArrayList<PendingReward> rewards = new ArrayList();
        PendingRewardIndex pendingRewardIndex = new PendingRewardIndex();
        pendingRewardIndex.distributionId(systemId);
        this.dataStore.createIfAbsent(pendingRewardIndex,true);
        //pendingRewardIndex.keySet().forEach(k->{
            //PendingReward pending = new PendingReward();
            //pending.distributionKey(k);
            //if(dataStore.load(pending)) rewards.add(pending);
        //});
        return rewards;
    }
}
