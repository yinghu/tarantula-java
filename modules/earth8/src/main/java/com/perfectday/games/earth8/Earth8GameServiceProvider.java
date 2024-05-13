package com.perfectday.games.earth8;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.icodesoftware.*;
import com.icodesoftware.protocol.*;
import com.icodesoftware.service.ApplicationPreSetup;
import com.icodesoftware.service.TokenValidatorProvider;
import com.icodesoftware.util.JsonUtil;

import com.icodesoftware.util.ScheduleRunner;
import com.icodesoftware.util.SnowflakeKey;
import com.perfectday.games.earth8.analytics.*;
import com.perfectday.games.earth8.inbox.PlayerAction;
import com.perfectday.games.earth8.inbox.PlayerActionQuery;
import com.perfectday.games.earth8.inbox.PlayerEventInbox;
import com.perfectday.games.earth8.data.PlayerDataTrack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class Earth8GameServiceProvider implements GameServiceProvider {

    private GameContext gameContext;
    private final static String ANALYTICS_QUERY_HEADER = "#Analytics";
    private final static long EVENT_DISPATCH_DELAY = 100; //100ms
    private String ANALYTICS_QUERY;

    private ConcurrentHashMap<Long,Tournament> tournamentIndex = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,ApplicationResource> resourceIndex = new ConcurrentHashMap<>();

    public void setup(GameContext gameContext){
        this.gameContext = gameContext;
        this.gameContext.registerTournamentListener(this);
        this.gameContext.recoverableRegistry(new Earth8PortableRegistry<>());
        ANALYTICS_QUERY = this.gameContext.applicationSchema().typeId()+ANALYTICS_QUERY_HEADER;
        this.gameContext.log("Start earth 8 game service provider with typeId : "+this.gameContext.applicationSchema().typeId(), OnLog.WARN);
    }

    //callbacks from HTTP
    @Override
    public void onJoined(Session session,Room room) {

        TokenValidatorProvider.AuthVendor webhook = gameContext.authorVendor(OnAccess.WEB_HOOK);
        PlayerDataTrack analyticsSession = PlayerDataTrack.lookup(gameContext,session.distributionId(), PlayerDataTrack.Type.Analytics);
        analyticsSession.trackId = gameContext.applicationSchema().applicationPreSetup().distributionId();
        analyticsSession.update();
        ServerConnectTransaction serverConnectTransaction = new ServerConnectTransaction(session,analyticsSession.trackId);
        gameContext.schedule(new ScheduleRunner(EVENT_DISPATCH_DELAY,()->
                webhook.upload(ANALYTICS_QUERY,serverConnectTransaction.toString().getBytes()))
        );
    }

    @Override
    public void onLeft(Session session) {

    }

    public void startGame(Session session, byte[] payload) throws Exception{
        long analyticsBatchId = gameContext.applicationSchema().applicationPreSetup().distributionId();
        BattleTransaction battleTransaction = BattleTransaction.fromJson(payload);
        if(!battleTransaction.validate()){
            session.write(JsonUtil.toSimpleResponse(false,"invalid battle settings").getBytes());
            return;
        }
        //single read to validate party items

        //if party check fail return false;
        Transaction transaction = gameContext.applicationSchema().transaction();
        boolean created = transaction.execute(ctx->{
            ApplicationPreSetup setup = (ApplicationPreSetup)ctx;
            DataStore dataStore = setup.onDataStore("battle");
            return dataStore.create(battleTransaction);
        });

        session.write(created ? battleTransaction.toJson().toString().getBytes() : JsonUtil.toSimpleResponse(false,"failed to create battle transaction").getBytes());
        TokenValidatorProvider.AuthVendor webhook = gameContext.authorVendor(OnAccess.WEB_HOOK);
        gameContext.schedule(new ScheduleRunner(EVENT_DISPATCH_DELAY,()-> {
            PlayerDataTrack serverSession = PlayerDataTrack.lookup(gameContext,session.distributionId(), PlayerDataTrack.Type.Analytics);
            webhook.upload(ANALYTICS_QUERY, new BattleStartTransaction(session,serverSession.trackId, battleTransaction.distributionId(), payload, analyticsBatchId).toString().getBytes());
            JsonObject jsonObject = JsonUtil.parse(payload);
            if (jsonObject.has("analytics")) {
                var analyticsData = AnalyticsBatchUtils.getAnalyticsData(jsonObject.getAsJsonArray("analytics"));
                PlayerDataTrack serverSessionTrack = PlayerDataTrack.lookup(gameContext,session.distributionId(), PlayerDataTrack.Type.Analytics);
                var transactions = AnalyticsBatchUtils.getTransactions(session,serverSessionTrack.trackId, analyticsBatchId, analyticsData);
                transactions.forEach(analytics -> webhook.upload(ANALYTICS_QUERY, analytics.toString().getBytes()));
            }
        }));
        gameContext.onMetrics("totalBattle",1);
    }

    public void updateGame(Session session,byte[] payload) throws Exception{
        BattleUpdate update = BattleUpdate.fromJson(payload);
        PlayerDataTrack serverSession = PlayerDataTrack.lookup(gameContext,session.distributionId(), PlayerDataTrack.Type.Analytics);

        if (update.score > 0 && update.playerLevel > 0) {

            var playerDataTrack = PlayerDataTrack.lookup(gameContext,session.distributionId(),PlayerDataTrack.Type.Tournament);

            Tournament existing = tournamentIndex.get(playerDataTrack.trackId);
            if(existing!=null){
                var totalScore = existing.register(session).update(session,entry->{
                    entry.score(0,update.score);
                    return true;
                });
                update.pendingAnalytics.add(new RLCPointsEarnedTransaction(session, serverSession.trackId, existing.distributionId(), update.objectiveType, update.score, totalScore));
            }
            else{
                scoreTournamentWithSameLevel(session, update, playerDataTrack, serverSession);
            }
        }

        if(update.update(gameContext.applicationSchema().applicationPreSetup(), session,serverSession.trackId,gameContext.applicationSchema().applicationPreSetup().distributionId()))
        {
            TokenValidatorProvider.AuthVendor webhook = gameContext.authorVendor(OnAccess.WEB_HOOK);
            gameContext.schedule(new ScheduleRunner(EVENT_DISPATCH_DELAY,()->
                    update.publishAnalytics(webhook,ANALYTICS_QUERY))
            );
        }
        session.write(JsonUtil.toSimpleResponse(true,"battle updated").getBytes());
    }

    public void endGame(Session session,byte[] payload) throws Exception{
        long analyticsBatchId = gameContext.applicationSchema().applicationPreSetup().distributionId();
        BattleTransaction battleTransaction = BattleTransaction.fromJson(payload);
        if(battleTransaction.distributionId()<=0){
            session.write(JsonUtil.toSimpleResponse(false,"invalid battleId").getBytes());
            return;
        }
        boolean win = battleTransaction.win;
        Transaction transaction = gameContext.applicationSchema().transaction();
        boolean updated = transaction.execute(ctx->{
            ApplicationPreSetup applicationPreSetup = (ApplicationPreSetup)ctx;
            DataStore dataStore = applicationPreSetup.onDataStore("battle");
            if(!dataStore.load(battleTransaction)) return false;
            battleTransaction.disabled(true);
            battleTransaction.win = win;
            dataStore.update(battleTransaction);
            resourceIndex.forEach((k,resource)->{
                ApplicationResource.Redeemer redeemer = gameContext.redeemer(session);
                redeemer.redeem(applicationPreSetup,resource);
            });
            return true;
        });

        session.write(JsonUtil.toSimpleResponse(updated,"battle finished").getBytes());
        TokenValidatorProvider.AuthVendor webhook = gameContext.authorVendor(OnAccess.WEB_HOOK);
        gameContext.schedule(new ScheduleRunner(EVENT_DISPATCH_DELAY,()-> {
            PlayerDataTrack serverSession = PlayerDataTrack.lookup(gameContext,session.distributionId(), PlayerDataTrack.Type.Analytics);
            webhook.upload(ANALYTICS_QUERY, new BattleEndTransaction(session,serverSession.trackId, battleTransaction.distributionId(), payload, analyticsBatchId).toBytes());
            JsonObject jsonObject = JsonUtil.parse(payload);
            if (jsonObject.has("analytics")) {
                var analyticsData = AnalyticsBatchUtils.getAnalyticsData(jsonObject.getAsJsonArray("analytics"));
                PlayerDataTrack serverSessionTrack = PlayerDataTrack.lookup(gameContext,session.distributionId(), PlayerDataTrack.Type.Analytics);
                var transactions = AnalyticsBatchUtils.getTransactions(session,serverSessionTrack.trackId, analyticsBatchId, analyticsData);
                transactions.forEach(analytics -> webhook.upload(ANALYTICS_QUERY, analytics.toString().getBytes()));
            }
        }));
    }

    //System level game event callbacks
    public <T extends OnAccess> void onGameEvent(T event){
        if(event.command().equals("ShippingFormCompleted")){
            if(gameContext.applicationSchema().transaction().execute(ctx->{
                var tournamentType = JsonUtil.parse((byte[])event.property(OnAccess.PAYLOAD)).get("tournament_type").getAsString();
                DataStore playerActionStore = ctx.onDataStore("player_coin_form");
                PlayerAction playerAction = new PlayerAction("ShippingFormCompleted-" + tournamentType,true);
                playerAction.ownerKey(SnowflakeKey.from(Long.parseLong(event.systemId())));
                return playerActionStore.create(playerAction);
            })){
                TokenValidatorProvider.AuthVendor webhook = gameContext.authorVendor(OnAccess.WEB_HOOK);
                gameContext.schedule(new ScheduleRunner(EVENT_DISPATCH_DELAY,()->
                        webhook.upload(ANALYTICS_QUERY, new ServerMetadataTransaction(event).toBytes())
                ));
            }
        }

    }

    public void onInventory(ApplicationPreSetup applicationPreSetup,Inventory inventory, Inventory.Stock stock){
        if(inventory.rechargeable()) return;
        if(inventory.type().equals("GameItem.Equipment")){
            Configurable configurable = applicationPreSetup.load(gameContext.applicationSchema().application("item"),stock.itemId());
            if(configurable==null) throw new RuntimeException("No configurable associated with inventory ["+inventory.type()+"]");
            DataStore dataStore = applicationPreSetup.onDataStore(Inventory.DataStore);
            Equipment equipment = Equipment.fromConfig(stock.stockId(),configurable);
            dataStore.createIfAbsent(equipment,false);
            if(inventory.stockFactoryId()==equipment.getFactoryId() && inventory.stockClassId()==equipment.getClassId()) return;
            inventory.stockFactoryId(equipment.getFactoryId());
            inventory.stockClassId(equipment.getClassId());
            dataStore.update(inventory);
            return;
        }
        if(inventory.type().equals("GameItem.Unit")){
            Configurable configurable = applicationPreSetup.load(gameContext.applicationSchema().application("item"),stock.itemId());
            if(configurable==null) throw new RuntimeException("No configurable associated with inventory ["+inventory.type()+"]");
            DataStore dataStore = applicationPreSetup.onDataStore(Inventory.DataStore);
            Unit unit = Unit.fromConfig(stock.stockId(),configurable);
            dataStore.createIfAbsent(unit,false);
            if(inventory.stockFactoryId()==unit.getFactoryId() && inventory.stockClassId()==unit.getClassId()) return;
            inventory.stockFactoryId(unit.getFactoryId());
            inventory.stockClassId(unit.getClassId());
            dataStore.update(inventory);
            return;
        }
        this.gameContext.log("Inventory type ["+inventory.type()+"] not supported",OnLog.WARN);
    }

    public List<OnInbox> inbox(Session session){
        List<OnInbox> inbox = new ArrayList<>();
        List<OnAccess> playerEvents = new ArrayList<>();
        gameContext.applicationSchema().transaction().execute(ctx->{
            DataStore dataStore = ctx.onDataStore("player_coin_form");
            dataStore.list(new PlayerActionQuery(session.distributionId())).forEach(playerAction -> {
                 playerEvents.add(playerAction);
            });
            return true;
        });
        inbox.add(new PlayerEventInbox("coinForm","tournament",playerEvents));
        return inbox;
    }

    @Override
    public void tournamentStarted(Tournament tournament) {
        gameContext.log("Tournament started : "+tournament.distributionId()+" : "+tournament.name()+" : "+tournament.type()+" : "+tournament.global(),OnLog.WARN);
        tournamentIndex.put(tournament.distributionId(),tournament);
        for(long start = tournament.startLevel();start<=tournament.endLevel();start++){
            tournamentIndex.put(start,tournament);
        }
        TokenValidatorProvider.AuthVendor webhook = gameContext.authorVendor(OnAccess.WEB_HOOK);
        gameContext.schedule(new ScheduleRunner(EVENT_DISPATCH_DELAY,()->
                webhook.upload(ANALYTICS_QUERY, new RLCTournamentStartTransaction(tournament.distributionId(), tournament.name()).toBytes())
        ));
    }

    @Override
    public void tournamentClosed(Tournament tournament) {
        gameContext.log("Tournament closed : "+tournament.distributionId()+" : "+tournament.name()+" : "+tournament.type()+" : "+tournament.global(),OnLog.WARN);
        tournamentIndex.remove(tournament.distributionId());
        for(long start = tournament.startLevel();start<=tournament.endLevel();start++){
            tournamentIndex.remove(start);
        }
        TokenValidatorProvider.AuthVendor webhook = gameContext.authorVendor(OnAccess.WEB_HOOK);
        gameContext.schedule(new ScheduleRunner(EVENT_DISPATCH_DELAY,()->
                webhook.upload(ANALYTICS_QUERY, new RLCTournamentEndTransaction(tournament.distributionId()).toBytes())
        ));
    }

    public void onApplicationResourceRegistered(ApplicationResource resource){
        this.resourceIndex.put(resource.name(),resource);
        this.gameContext.log(resource.name()+" registered",OnLog.INFO);
    }
    public void onApplicationResourceReleased(ApplicationResource resource){
        this.resourceIndex.remove(resource.name());
        this.gameContext.log(resource.name()+" released",OnLog.INFO);
    }

    @Override
    public void tournamentEnded(Tournament tournament) {
        tournamentIndex.remove(tournament.distributionId());
    }

    //UDP Channel Listener Callbacks
    public void onValidated(Channel channel){
        //UDP channel connection validation callback
    }
    public void onJoined(Channel channel){
        //UDP channel join callback
    }
    public void onLeft(Channel channel){
        //UDP channel disconnect callback
    }

    //Callbacks from UDP channel
    @Override
    public byte[] onRequest(Session session, MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        //UDP REQUEST RESPONSE CAN BE REPACKING FOR LARGE PAYLOAD
        return null;
    }

    @Override
    public void onAction(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer, UDPEndpointServiceProvider.RelayListener callback) {
        //UDP MESSAGE WITH RELAY CALL WITH SINGLE UDP MESSAGE
    }

    private void scoreTournamentWithSameLevel(Session session, BattleUpdate update, PlayerDataTrack playerDataTrack, PlayerDataTrack serverSession) {
        Tournament nextLevel = tournamentIndex.get(update.playerLevel);
        if(nextLevel==null) return;
        var tournamentInstance = nextLevel.register(session);
        update.pendingAnalytics.add(new RLCLeaderboardAssignedTransaction(session, serverSession.trackId, nextLevel.distributionId(), tournamentInstance.distributionId()));
        var totalScore = tournamentInstance.update(session,entry->{
            entry.score(0,update.score);
            return true;
        });
        update.pendingAnalytics.add(new RLCPointsEarnedTransaction(session, serverSession.trackId, nextLevel.distributionId(), update.objectiveType, update.score, totalScore));
        playerDataTrack.trackId = nextLevel.distributionId();
        playerDataTrack.update();
    }
}
