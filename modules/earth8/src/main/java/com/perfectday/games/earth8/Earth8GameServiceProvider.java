package com.perfectday.games.earth8;


import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.protocol.*;
import com.icodesoftware.service.ApplicationPreSetup;
import com.icodesoftware.service.TokenValidatorProvider;
import com.icodesoftware.util.JsonUtil;

import com.perfectday.games.earth8.analytics.BattleEndTransaction;
import com.perfectday.games.earth8.analytics.BattleStartTransaction;
import com.perfectday.games.earth8.analytics.ServerConnectTransaction;

import java.util.concurrent.ConcurrentHashMap;


public class Earth8GameServiceProvider implements GameServiceProvider {

    private GameContext gameContext;

    public final static String ANALYTICS_QUERY = "earth8#Analytics";

    private ConcurrentHashMap<Long,Tournament> tournamentIndex = new ConcurrentHashMap<>();
    public void setup(GameContext gameContext){
        this.gameContext = gameContext;
        this.gameContext.registerTournamentListener(this);
        this.gameContext.recoverableRegistry(new Earth8PortableRegistry<>());
        this.gameContext.log("Start earth 8 game service provider", OnLog.WARN);
    }

    //callbacks from HTTP
    @Override
    public void onJoined(Session session,Room room) {
        //use room.distributionId/key as game session id
        TokenValidatorProvider.AuthVendor webhook = gameContext.authorVendor(OnAccess.WEB_HOOK);
        webhook.upload(ANALYTICS_QUERY,new ServerConnectTransaction(session).toString().getBytes());
        this.gameContext.log("JOINED : "+room.distributionKey(), OnLog.WARN);
    }

    public void startGame(Session session, byte[] payload) throws Exception{
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
        webhook.upload(ANALYTICS_QUERY,new BattleStartTransaction(session, battleTransaction.distributionId(), payload).toString().getBytes());
//        gameContext.onMetrics("totalKills",100);
//        gameContext.onMetrics("totalWins",10);
//        gameContext.onMetrics("totalRounds",20);
        gameContext.onMetrics("totalBattle",1);
    }

    public void updateGame(Session session,byte[] payload) throws Exception{
        BattleUpdate update = BattleUpdate.fromJson(payload);
//        gameContext.log("Update Game : " + update.updateId, OnLog.INFO);
        Transaction transaction = gameContext.applicationSchema().transaction();
        boolean updated = transaction.execute(ctx->{
            ApplicationPreSetup applicationPreSetup = (ApplicationPreSetup)ctx;
            DataStore dataStore = applicationPreSetup.onDataStore("battle");
            if(!dataStore.create(update)) return false;
            //TO MORE TRANSACTION STUFF
            return update.update(applicationPreSetup, session);
        });

        if(updated)
        {
            TokenValidatorProvider.AuthVendor webhook = gameContext.authorVendor(OnAccess.WEB_HOOK);
            update.publishAnalytics(webhook);
        }
        session.write(JsonUtil.toSimpleResponse(updated,updated?"battle updated":"failed to update").getBytes());
    }

    public void endGame(Session session,byte[] payload) throws Exception{
        JsonObject jsonObject = JsonUtil.parse(payload);
        long battleId = jsonObject.get("BattleId").getAsLong();
        boolean win = jsonObject.get("Win").getAsBoolean();

        if(battleId<=0){
            session.write(JsonUtil.toSimpleResponse(false,"invalid battleId").getBytes());
            return;
        }
        BattleTransaction battleTransaction = new BattleTransaction();
        battleTransaction.distributionId(battleId);
        Transaction transaction = gameContext.applicationSchema().transaction();
        boolean updated = transaction.execute(ctx->{
            ApplicationPreSetup applicationPreSetup = (ApplicationPreSetup)ctx;
            DataStore dataStore = applicationPreSetup.onDataStore("battle");
            if(!dataStore.load(battleTransaction)) return false;
            battleTransaction.win = win;
            battleTransaction.disabled(true);
            return dataStore.update(battleTransaction);
        });

        session.write(JsonUtil.toSimpleResponse(updated,"battle finished").getBytes());
        TokenValidatorProvider.AuthVendor webhook = gameContext.authorVendor(OnAccess.WEB_HOOK);
        webhook.upload(ANALYTICS_QUERY,new BattleEndTransaction(session, battleTransaction.distributionId(), payload).toString().getBytes());
    }
    public <T extends OnAccess> void onGameEvent(T event){
        gameContext.log("EVENT : "+event.toJson().toString(),OnLog.WARN);
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
    @Override
    public void onLeft(Session session) {
        gameContext.log(" LEAVE : "+session.distributionKey()+" : "+session.stub(),OnLog.WARN);
    }

    //Callbacks from UDP channel
    @Override
    public byte[] onRequest(Session session, MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        //UDP REQUEST RESPONSE CAN BE REPACKING FOR LARGE PAYLOAD
        this.gameContext.log("On Request : "+session.distributionKey(), OnLog.WARN);

        //Statistics statistics = gameContext.statistics(session);
        //statistics.entry("kills").update(1).update();
        //this.gameContext.log("On Statistics : "+statistics.entry("kills").total(), OnLog.WARN);
        return null;//callback on caller only if byte not null
    }

    @Override
    public void onAction(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer, UDPEndpointServiceProvider.RelayListener callback) {
        //UDP MESSAGE WITH RELAY CALL WITH SINGLE UDP MESSAGE
        this.gameContext.log("On Action : "+messageHeader.sessionId, OnLog.WARN);
        //read buffer -> write header/buffer->flip->read header->callback on channel members
    }

    @Override
    public void tournamentStarted(Tournament tournament) {
        gameContext.log("Tournament started : "+tournament.distributionId()+" : "+tournament.name()+" : "+tournament.type()+" : "+tournament.global(),OnLog.WARN);
        tournamentIndex.put(tournament.distributionId(),tournament);
    }

    @Override
    public void tournamentClosed(Tournament tournament) {
        gameContext.log("Tournament closed : "+tournament.distributionId()+" : "+tournament.name()+" : "+tournament.type()+" : "+tournament.global(),OnLog.WARN);
        tournamentIndex.remove(tournament.distributionId());
    }

    @Override
    public void tournamentEnded(Tournament tournament) {
        tournamentIndex.remove(tournament.distributionId());
    }
}
