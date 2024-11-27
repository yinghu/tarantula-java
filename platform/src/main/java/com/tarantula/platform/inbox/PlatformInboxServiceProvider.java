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
    private PlatformTournamentServiceProvider tournamentServiceProvider;

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

    public void checkGlobalItemGrant(Session session){
        //Get Player Level and Account Creation Date
        String[] payloadSplit = session.name().split("#");
        int playerLevel = Integer.parseInt(payloadSplit[1]);
        LocalDate accountCreatedDate = LocalDate.parse(payloadSplit[0], DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        //Date Stores
        DataStore globalDataStore = applicationPreSetup.dataStore(gameCluster,"global_item_grant_events");
        DataStore playerDataStore = gameCluster.applicationPreSetup().onDataStore("player_item_grant_events");
        List<LocalDateTime> playerGlobalGrantList = new ArrayList<>();

        //Get All Global Item Grants From Player
        playerDataStore.list(new PlatformItemGrantEventQuery(session.distributionId())).forEach(itemGrantEvent -> {
            if(itemGrantEvent.type.equals("Global")){
                playerGlobalGrantList.add(itemGrantEvent.dateCreated);
            }
        });

        //Check For New Global Item Grant Events Not In Players List
        globalDataStore.list(new GlobalItemGrantEventQuery(gameCluster.distributionId())).forEach(globalGrantEvent -> {
            if(globalGrantEvent.completed) return;

            if(!playerGlobalGrantList.contains(globalGrantEvent.dateCreated)){
                boolean shouldComplete = false;

                //Player Level Filter
                if(playerLevel < globalGrantEvent.minPlayerLevelFilter || playerLevel > globalGrantEvent.maxPlayerLevelFilter){
                    shouldComplete = true;
                }

                //Account Creation Date Filter
                if(accountCreatedDate.isBefore(globalGrantEvent.minInstallDateFilter) || accountCreatedDate.isAfter(globalGrantEvent.maxInstallDateFilter)){
                    shouldComplete = true;
                }

                //Tournament Filter
                if(globalGrantEvent.tournamentIdFilter != 0){
                    Tournament tournament = tournamentServiceProvider.tournament(globalGrantEvent.tournamentIdFilter);
                    if(!tournament.isPlayerEnteredInTournament(session)){
                        shouldComplete = true;
                    }
                }

                //Create New ItemGrantEvent For Player
                PlatformItemGrantEvent itemGrantEvent = new PlatformItemGrantEvent("Global", globalGrantEvent.itemID, globalGrantEvent.itemName, globalGrantEvent.amount, shouldComplete, globalGrantEvent.dateCreated);
                itemGrantEvent.ownerKey(SnowflakeKey.from(session.distributionId()));
                playerDataStore.create(itemGrantEvent);
            }
        });
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
        this.tournamentServiceProvider = platformGameServiceProvider.tournamentServiceProvider();
        logger.warn("Platform inbox started->"+gameServiceName);
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
