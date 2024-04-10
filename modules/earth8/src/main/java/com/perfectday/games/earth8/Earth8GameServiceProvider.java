package com.perfectday.games.earth8;

import com.google.gson.JsonObject;
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
import com.perfectday.games.earth8.data.PlayerDataTrackQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
        //use room.distributionId/key as game session id
        TokenValidatorProvider.AuthVendor webhook = gameContext.authorVendor(OnAccess.WEB_HOOK);
        gameContext.schedule(new ScheduleRunner(EVENT_DISPATCH_DELAY,()->
                webhook.upload(ANALYTICS_QUERY,new ServerConnectTransaction(session).toString().getBytes()))
        );
    }

    @Override
    public void onLeft(Session session) {

    }

    public void startGame(Session session, byte[] payload) throws Exception{
        UUID analyticsBatchId = AnalyticsBatchUtils.generateAnalyticsBatchId();
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
            webhook.upload(ANALYTICS_QUERY, new BattleStartTransaction(session, battleTransaction.distributionId(), payload, analyticsBatchId).toString().getBytes());
            JsonObject jsonObject = JsonUtil.parse(payload);
            if (jsonObject.has("analytics")) {
                var analyticsData = AnalyticsBatchUtils.getAnalyticsData(jsonObject.getAsJsonArray("analytics"));
                var transactions = AnalyticsBatchUtils.getTransactions(session, analyticsBatchId, analyticsData);
                transactions.forEach(analytics -> webhook.upload(ANALYTICS_QUERY, analytics.toString().getBytes()));
            }
        }));
        gameContext.onMetrics("totalBattle",1);
    }

    public void updateGame(Session session,byte[] payload) throws Exception{
        BattleUpdate update = BattleUpdate.fromJson(payload);
        Transaction transaction = gameContext.applicationSchema().transaction();
        boolean updated = transaction.execute(ctx->{
            ApplicationPreSetup applicationPreSetup = (ApplicationPreSetup)ctx;
            DataStore dataStore = applicationPreSetup.onDataStore("battle");
            if(!dataStore.create(update)) return false;

            if (update.score > 0 && update.playerLevel > 0) {
                DataStore tournamentTrackDataStore = applicationPreSetup.onDataStore("player_tournament_track");
                var playerDataTracks = tournamentTrackDataStore.list(new PlayerDataTrackQuery(session.distributionId()));

                if(playerDataTracks.isEmpty()){
                    scoreTournamentWithSameLevel(session, update, tournamentTrackDataStore);
                }
                else{
                    PlayerDataTrack playerDataTrack = playerDataTracks.get(0);
                    Tournament existing = tournamentIndex.get(playerDataTrack.tournamentId);
                    if(existing!=null){
                        existing.register(session).update(session,entry->{
                            entry.score(0,update.score);
                            return true;
                        });
                    }
                    else{
                        if (scoreTournamentWithSameLevel(session, update, tournamentTrackDataStore)){
                            for (var track : playerDataTracks) {
                                tournamentTrackDataStore.delete(track);
                            }
                        }
                    }
                }
            }

            //TO MORE TRANSACTION STUFF
            return update.update(applicationPreSetup, session);
        });



        if(updated)
        {
            TokenValidatorProvider.AuthVendor webhook = gameContext.authorVendor(OnAccess.WEB_HOOK);
            gameContext.schedule(new ScheduleRunner(EVENT_DISPATCH_DELAY,()->
                    update.publishAnalytics(webhook,ANALYTICS_QUERY))
            );
        }
        session.write(JsonUtil.toSimpleResponse(updated,updated?"battle updated":"failed to update").getBytes());
    }

    public void endGame(Session session,byte[] payload) throws Exception{
        UUID analyticsBatchId = AnalyticsBatchUtils.generateAnalyticsBatchId();
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
            /** Inventory item remove from the Inventory
             * Rechargeable item  cannot be removed
            Inventory inventory = applicationPreSetup.inventory(session.distributionId(),"Hero");
            Inventory.Stock removed = inventory.stock(1234);
            if(removed!=null){
                inventory.removeStock(removed);
            }
            **/
            return true;
        });

        session.write(JsonUtil.toSimpleResponse(updated,"battle finished").getBytes());
        TokenValidatorProvider.AuthVendor webhook = gameContext.authorVendor(OnAccess.WEB_HOOK);
        gameContext.schedule(new ScheduleRunner(EVENT_DISPATCH_DELAY,()-> {
            webhook.upload(ANALYTICS_QUERY, new BattleEndTransaction(session, battleTransaction.distributionId(), payload, analyticsBatchId).toBytes());
            JsonObject jsonObject = JsonUtil.parse(payload);
            if (jsonObject.has("analytics")) {
                var analyticsData = AnalyticsBatchUtils.getAnalyticsData(jsonObject.getAsJsonArray("analytics"));
                var transactions = AnalyticsBatchUtils.getTransactions(session, analyticsBatchId, analyticsData);
                transactions.forEach(analytics -> webhook.upload(ANALYTICS_QUERY, analytics.toString().getBytes()));
            }
        }));
    }

    //System level game event callbacks
    public <T extends OnAccess> void onGameEvent(T event){
        if(event.command().equals("ShippingFormCompleted")){
            if(gameContext.applicationSchema().transaction().execute(ctx->{
                DataStore playerActionStore = ctx.onDataStore("player_coin_form");
                PlayerAction playerAction = new PlayerAction("ShippingFormCompleted",true);
                playerAction.ownerKey(SnowflakeKey.from(Long.parseLong(event.systemId())));
                return playerActionStore.create(playerAction);
            })){
                TokenValidatorProvider.AuthVendor webhook = gameContext.authorVendor(OnAccess.WEB_HOOK);
                gameContext.schedule(new ScheduleRunner(EVENT_DISPATCH_DELAY,()->
                        webhook.upload(ANALYTICS_QUERY, new ServerMetadataTransaction(event).toBytes())
                ));
            }
        }
        else if(event.command().equals("something else")){

        }
        //PENDING REMOVE
        var eventType = (String)event.property(OnAccess.TYPE_ID);
        if(eventType != null && eventType.equals("onAction"))
        {
            var data = (byte[])event.property(OnAccess.PAYLOAD);
            var jsonData = JsonUtil.parse(data);
            var playerId = JsonUtil.getJsonLong(jsonData, "playerId", 0);
            if(playerId>0){
                gameContext.applicationSchema().transaction().execute(ctx->{
                    DataStore playerActionStore = ctx.onDataStore("player_coin_form");
                    PlayerAction playerAction = new PlayerAction("ShippingFormCompleted",true);
                    playerAction.ownerKey(SnowflakeKey.from(playerId));
                    return playerActionStore.create(playerAction);
                });
            }
        }
        TokenValidatorProvider.AuthVendor webhook = gameContext.authorVendor(OnAccess.WEB_HOOK);
        gameContext.schedule(new ScheduleRunner(EVENT_DISPATCH_DELAY,()->
                webhook.upload(ANALYTICS_QUERY, new ServerMetadataTransaction(event).toBytes())
        ));
        //END OF PENDING REMOVE
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
    }

    @Override
    public void tournamentClosed(Tournament tournament) {
        gameContext.log("Tournament closed : "+tournament.distributionId()+" : "+tournament.name()+" : "+tournament.type()+" : "+tournament.global(),OnLog.WARN);
        tournamentIndex.remove(tournament.distributionId());
        for(long start = tournament.startLevel();start<=tournament.endLevel();start++){
            tournamentIndex.remove(start);
        }
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

    private boolean scoreTournamentWithSameLevel(Session session, BattleUpdate update, DataStore tournamentTrackDataStore) {
        Tournament nextLevel = tournamentIndex.get(update.playerLevel);
        if(nextLevel!=null){
            nextLevel.register(session).update(session,entry->{
                entry.score(0,update.score);
                return true;
            });
            var tournamentTrack = new PlayerDataTrack(nextLevel.distributionId());
            tournamentTrack.ownerKey(SnowflakeKey.from(session.distributionId()));
            tournamentTrackDataStore.create(tournamentTrack);
            return true;
        }
        return false;
    }
}
