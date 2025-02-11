package com.tarantula.platform.inbox;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.SnowflakeKey;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.game.service.PlatformGameServiceSetup;

import com.tarantula.platform.configuration.MailboxCredentialConfiguration;
import com.tarantula.platform.inventory.PlatformInventoryServiceProvider;
import com.tarantula.platform.item.Application;
import com.tarantula.platform.tournament.PlatformTournamentServiceProvider;
import com.tarantula.platform.tournament.TournamentPrize;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        inbox.inboxList = this.platformGameServiceProvider.gameServiceProvider().inbox(session);
        return inbox;
    }



    public Mailbox mailbox(Session session){
        Map<Long,MailboxCredentialConfiguration> inbox = this.platformGameServiceProvider.configurationServiceProvider().inbox();
        Mailbox mailbox = new Mailbox();
        if(inbox.size()==0) return mailbox;
        String locId  = session.name();
        inbox.forEach((k,v)->{
            if(TimeUtil.expired(v.startTime()) && !TimeUtil.expired(v.expirationTime())){
                Announcement announcement = v.announcement(locId);
                if(announcement!=null){
                    announcement.startTime = v.startTime();
                    announcement.distributionId(k);
                    mailbox.announcementList.add(announcement);
                }
            }
        });
        return mailbox;
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        super.setup(serviceContext);
        this.dataStore = applicationPreSetup.dataStore(gameCluster,NAME);
        Configuration configuration = serviceContext.configuration("game-presence-settings");
        JsonObject inbox = ((JsonElement)configuration.property("inbox")).getAsJsonObject();
        pendingReward = inbox.get("pendingReward").getAsBoolean();
        this.logger = JDKLogger.getLogger(PlatformInboxServiceProvider.class);
        logger.info("Platform inbox started->"+gameServiceName);
    }

    public <T extends Application> void claim(long systemId,T item){
        if(pendingReward){
            PendingReward pending = new PendingReward(item);
            pending.ownerKey(SnowflakeKey.from(systemId));
            this.dataStore.create(pending);
            return;
        }
        this.inventoryServiceProvider.redeem(Long.toString(systemId),item);
    }

    public void pendingTournamentPrize(long systemId,TournamentPrize tournamentPrize){
        if(pendingReward){
            PendingReward pending = new PendingReward(tournamentPrize);
            pending.ownerKey(SnowflakeKey.from(systemId));
            this.dataStore.create(pending);
            return;
        }
        this.inventoryServiceProvider.redeem(Long.toString(systemId),tournamentPrize);
    }

    public boolean redeem(Session session,String rewardKey){
        PendingReward reward = new PendingReward();
        reward.distributionKey(rewardKey);
        if(!this.dataStore.load(reward) || reward.disabled()) return false;
        if(!inventoryServiceProvider.redeem(session.systemId(),reward.toApplication())) return false;
        reward.disabled(true);
        dataStore.update(reward);
        return true;
    }
    private List<PendingReward> rewardList(long systemId){
        ArrayList<PendingReward> rewards = new ArrayList();
        dataStore.list(new PendingRewardQuery(systemId),(reward)->{
            if(!reward.disabled()) rewards.add(reward);
            return true;
        });
        return rewards;
    }
}
