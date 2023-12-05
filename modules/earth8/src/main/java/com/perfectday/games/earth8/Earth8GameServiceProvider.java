package com.perfectday.games.earth8;


import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.protocol.*;
import com.icodesoftware.service.ApplicationPreSetup;
import com.icodesoftware.service.TokenValidatorProvider;
import com.icodesoftware.util.JsonUtil;

import com.perfectday.games.earth8.analytics.BattleEndTransaction;
import com.perfectday.games.earth8.analytics.BattleStartTransaction;

import java.util.concurrent.ConcurrentHashMap;


public class Earth8GameServiceProvider implements GameServiceProvider {

    private GameContext gameContext;

    public final static String ANALYTICS_QUERY = "earth8#Analytics";

    private ConcurrentHashMap<Long,Tournament> tournamentIndex = new ConcurrentHashMap<>();
    public void setup(GameContext gameContext){
        this.gameContext = gameContext;
        this.gameContext.registerTournamentListener(this);
        this.gameContext.log("Start earth 8 game service provider", OnLog.WARN);
    }

    //callbacks from HTTP
    @Override
    public void onJoined(Session session) {
        gameContext.log("JOIN : "+session.distributionKey()+" :"+session.stub(),OnLog.WARN);
        tournamentIndex.forEach((key,entry)->{
            if(entry.type().equals("Q100")){//LEVEL UP GLOBAL TOURNAMENT
                entry.register(session).update(session,(e)->{
                    e.score(10,100);
                    return true;
                });
            }
        });
    }

    public void startGame(Session session, byte[] payload) throws Exception{
        BattleTransaction battleTransaction = BattleTransaction.fromJson(payload);
        if(!battleTransaction.validate()){
            session.write(JsonUtil.toSimpleResponse(false,"invalid battle settings").getBytes());
            return;
        }
        //single read to validate party items
        ApplicationPreSetup applicationPreSetup = gameContext.applicationSchema().applicationPreSetup();
        //applicationPreSetup.list()
        //Inventory gem = applicationPreSetup.inventory(session.distributionId(),"Unit");
        //if(gem!=null) this.gameContext.log(gem.balance()+" : "+gem.rechargeable()+" : "+gem.count(0),OnLog.WARN);
        applicationPreSetup.inventoryList(session.distributionId()).forEach(t->{
            t.onStock().forEach(configurable -> {
                this.gameContext.log(configurable.distributionId()+" : "+configurable.stockId()+" : "+t.type(),OnLog.WARN);

                //Configurable stock = applicationPreSetup.load(gameContext.applicationSchema().application("item"),configurable.stockId());
                //this.gameContext.log(stock.header().toString(),OnLog.WARN);
                //this.gameContext.log(stock.application().toString(),OnLog.WARN);
                //this.gameContext.log(stock.reference().toString(),OnLog.WARN);
                //stock.setup();
                //this.gameContext.log(stock.toJson().toString(),OnLog.WARN);
            });
        });
        //if party check fail return false;
        Transaction transaction = gameContext.applicationSchema().transaction();
        boolean created = transaction.execute(ctx->{
            ApplicationPreSetup setup = (ApplicationPreSetup)ctx;
            DataStore dataStore = setup.onDataStore("battle");
            return dataStore.create(battleTransaction);
        });
        tournamentIndex.forEach((key,entry)->{
            gameContext.log(entry.register(session).raceBoard().toJson().toString(),OnLog.WARN);
        });
        session.write(created ? battleTransaction.toJson().toString().getBytes() : JsonUtil.toSimpleResponse(false,"failed to create battle transaction").getBytes());
        TokenValidatorProvider.AuthVendor webhook = gameContext.authorVendor(OnAccess.WEB_HOOK);
        webhook.upload(ANALYTICS_QUERY,new BattleStartTransaction(session, battleTransaction.distributionId(), payload).toString().getBytes());
    }

    public void updateGame(Session session,byte[] payload) throws Exception{
        BattleUpdate update = BattleUpdate.fromJson(payload);
        Transaction transaction = gameContext.applicationSchema().transaction();
        boolean updated = transaction.execute(ctx->{
            ApplicationPreSetup applicationPreSetup = (ApplicationPreSetup)ctx;
            DataStore dataStore = applicationPreSetup.onDataStore("battle");
            if(!dataStore.create(update)) return false;
            //TO MORE TRANSACTION STUFF
            return update.update(applicationPreSetup, session);
        });
        if(updated){//do http calls outside transaction operations
            TokenValidatorProvider.AuthVendor webhook = gameContext.authorVendor(OnAccess.WEB_HOOK);
            update.publishAnalytics(webhook);
        }
        session.write(JsonUtil.toSimpleResponse(updated,updated?"battle updated":"failed to update").getBytes());
    }

    public void endGame(Session session,byte[] payload) throws Exception{
        JsonObject jsonObject = JsonUtil.parse(payload);
        long battleId = jsonObject.get("BattleId").getAsLong();
        boolean win = jsonObject.get("Win").getAsBoolean();
        int level = jsonObject.get("Level").getAsInt();

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
        if(updated){
            tournamentIndex.forEach((key,entry)->{
                if(entry.type().equals("T100")){//LEVEL UP GLOBAL TOURNAMENT
                    entry.register(session).update(session,(e)->{
                        e.score(0,level);
                        return true;
                    });
                }
            });
            //TO DO AFTER CURRENT BATTLE FINISHED
        }
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
        return null;//callback on caller only if byte not null
    }

    @Override
    public void onAction(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer, UDPEndpointServiceProvider.RelayListener callback) {
        //UDP MESSAGE WITH RELAY CALL WITH SINGLE UDP MESSAGE
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
