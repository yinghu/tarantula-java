package com.tarantula.platform.inbox;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.game.service.PlatformGameServiceSetup;

import com.tarantula.platform.inventory.PlatformInventoryServiceProvider;
import com.tarantula.platform.item.Application;

import java.util.ArrayList;
import java.util.List;

public class PlatformInboxServiceProvider extends PlatformGameServiceSetup {

    public static final String NAME = "inbox";


    private final PlatformInventoryServiceProvider inventoryServiceProvider;

    private boolean pendingReward;
    public PlatformInboxServiceProvider(PlatformGameServiceProvider gameServiceProvider){
        super(gameServiceProvider,NAME);
        this.inventoryServiceProvider = gameServiceProvider.inventoryServiceProvider();
    }


    public Inbox inbox(String systemId){
        Inbox inbox = new Inbox();
        inbox.inventoryList = this.inventoryServiceProvider.inventoryList(systemId);
        inbox.rewardList = this.rewardList(systemId);
        return inbox;
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        super.setup(serviceContext);
        this.dataStore = applicationPreSetup.dataStore(gameCluster,NAME);
        Configuration configuration = serviceContext.configuration("game-presence-settings");
        JsonObject inbox = ((JsonElement)configuration.property("inbox")).getAsJsonObject();
        pendingReward = inbox.get("pendingReward").getAsBoolean();
        this.logger = this.serviceContext.logger(PlatformInboxServiceProvider.class);
        logger.warn("Platform inbox started->"+gameServiceName);
    }

    public <T extends Application> void claim(String systemId,T item){
        if(pendingReward){
            PendingRewardIndex pendingRewardIndex = new PendingRewardIndex();
            pendingRewardIndex.distributionKey(systemId);
            this.dataStore.createIfAbsent(pendingRewardIndex,true);
            PendingReward pending = new PendingReward();
            pending.index(item.distributionKey());
            pending.name(item.configurationCategory());
            this.dataStore.create(pending);
            pendingRewardIndex.addKey(pending.distributionKey());
            this.dataStore.update(pendingRewardIndex);
            return;
        }
        this.inventoryServiceProvider.redeem(systemId,item);
    }
    public List<PendingReward> rewardList(String systemId){
        ArrayList<PendingReward> rewards = new ArrayList();
        PendingRewardIndex pendingRewardIndex = new PendingRewardIndex();
        pendingRewardIndex.distributionKey(systemId);
        this.dataStore.createIfAbsent(pendingRewardIndex,true);
        pendingRewardIndex.keySet().forEach(k->{
            PendingReward pending = new PendingReward();
            pending.distributionKey(k);
            if(dataStore.load(pending)) rewards.add(pending);
        });
        return rewards;
    }
}
