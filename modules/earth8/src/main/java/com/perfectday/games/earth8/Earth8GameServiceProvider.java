package com.perfectday.games.earth8;

import com.icodesoftware.*;
import com.icodesoftware.protocol.*;
import com.icodesoftware.service.ApplicationPreSetup;
import com.icodesoftware.service.TokenValidatorProvider;
import com.icodesoftware.util.JsonUtil;

import com.icodesoftware.util.ScheduleRunner;
import com.perfectday.games.earth8.analytics.BattleEndTransaction;
import com.perfectday.games.earth8.analytics.BattleStartTransaction;
import com.perfectday.games.earth8.analytics.ServerConnectTransaction;

import java.util.concurrent.ConcurrentHashMap;


public class Earth8GameServiceProvider implements GameServiceProvider {

    private GameContext gameContext;
    private final static String ANALYTICS_QUERY_HEADER = "#Analytics";
    private final static long EVENT_DISPATCH_DELAY = 100; //100ms
    private String ANALYTICS_QUERY;

    private ConcurrentHashMap<Long,Tournament> tournamentIndex = new ConcurrentHashMap<>();
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
        gameContext.schedule(new ScheduleRunner(EVENT_DISPATCH_DELAY,()->
                webhook.upload(ANALYTICS_QUERY,new BattleStartTransaction(session, battleTransaction.distributionId(), payload).toString().getBytes()))
        );
        gameContext.onMetrics("totalBattle",1);
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
            return dataStore.update(battleTransaction);
        });
        if(updated
            && battleTransaction.TEMP_BattleStage.equals("Chapter3_Stage7_HardConfig")
            && battleTransaction.win
        ) {
            // hard coded 7 day tournament completion
            tournamentIndex.forEach((key,entry)->{
                if(entry.type().startsWith("SevenDayTournament")) {
                    // register this user to the tournament the first time they finish the campaign
                    entry.register(session).update(session,(e)->{
                        this.gameContext.log("Test Register Player to tournament", OnLog.INFO);
                        if(e.score() > 0)
                        {
                            this.gameContext.log("Player already registered", OnLog.INFO);
                            return false;
                        }

                        this.gameContext.log("Player registering", OnLog.INFO);
                        e.score(0,1);
                        return true;
                    });
                }
            });
        }

        session.write(JsonUtil.toSimpleResponse(updated,"battle finished").getBytes());
        TokenValidatorProvider.AuthVendor webhook = gameContext.authorVendor(OnAccess.WEB_HOOK);
        gameContext.schedule(new ScheduleRunner(EVENT_DISPATCH_DELAY,()->
                webhook.upload(ANALYTICS_QUERY,new BattleEndTransaction(session, battleTransaction.distributionId(), payload).toString().getBytes()))
        );
    }

    //System level game event callbacks
    public <T extends OnAccess> void onGameEvent(T event){
        TokenValidatorProvider.AuthVendor webhook = gameContext.authorVendor(OnAccess.WEB_HOOK);
        gameContext.schedule(new ScheduleRunner(EVENT_DISPATCH_DELAY,()->
                webhook.upload(ANALYTICS_QUERY,event.toJson().toString().getBytes()))
        );
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
}
