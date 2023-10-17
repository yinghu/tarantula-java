package com.perfectday.games.earth8;


import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.protocol.*;
import com.icodesoftware.service.ApplicationPreSetup;
import com.icodesoftware.util.JsonUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Earth8GameServiceProvider implements GameServiceProvider {

    private GameContext gameContext;
    public void setup(GameContext gameContext){
        this.gameContext = gameContext;
        this.gameContext.log("Start earth 8 game service provider", OnLog.WARN);
    }

    //callbacks from HTTP
    @Override
    public void onJoined(Session session) {
        gameContext.log("JOIN : "+session.distributionKey()+" :"+session.stub(),OnLog.WARN);
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
        Inventory gem = applicationPreSetup.inventory(session.distributionId(),"Unit");
        if(gem!=null) this.gameContext.log(gem.balance()+" : "+gem.rechargeable()+" : "+gem.count(0),OnLog.WARN);
        applicationPreSetup.inventoryList(session.distributionId()).forEach(t->{
            t.onStock().forEach(configurable -> {
                this.gameContext.log(configurable.distributionId()+" : "+configurable.stockId(),OnLog.WARN);
                Configurable stock = applicationPreSetup.load(gameContext.applicationSchema().application("item"),configurable.stockId());
                this.gameContext.log(stock.header().toString(),OnLog.WARN);
                this.gameContext.log(stock.application().toString(),OnLog.WARN);
                this.gameContext.log(stock.reference().toString(),OnLog.WARN);
                stock.setup();
                this.gameContext.log(stock.toJson().toString(),OnLog.WARN);
            });
        });
        //if party check fail return false;
        Transaction transaction = gameContext.applicationSchema().transaction();
        boolean created = transaction.execute(ctx->{
            ApplicationPreSetup setup = (ApplicationPreSetup)ctx;
            DataStore dataStore = setup.onDataStore("battle");
            return dataStore.create(battleTransaction);
        });
        session.write(created?battleTransaction.toJson().toString().getBytes():JsonUtil.toSimpleResponse(false,"failed to create battle transaction").getBytes());
    }
    public void updateGame(Session session,byte[] payload) throws Exception{
        BattleUpdate update = BattleUpdate.fromJson(payload);
        Transaction transaction = gameContext.applicationSchema().transaction();
        boolean updated = transaction.execute(ctx->{
            ApplicationPreSetup applicationPreSetup = (ApplicationPreSetup)ctx;
            DataStore dataStore = applicationPreSetup.onDataStore("battle");
            if(!dataStore.create(update)) return false;
            //TO MORE TRANSACTION STUFF
            return update.update(applicationPreSetup);
        });
        session.write(JsonUtil.toSimpleResponse(updated,updated?"battle updated":"failed to update").getBytes());
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        gameContext.authorVendor(OnAccess.AMAZON).upload("earth8#"+"earth8/"+date+"/"+update.distributionKey()+".json",payload);
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
        if(updated){
            //TO DO AFTER CURRENT BATTLE FINISHED
        }
        session.write(JsonUtil.toSimpleResponse(updated,"battle finished").getBytes());
    }
    public <T extends OnAccess> void onGameEvent(T event){
        gameContext.log("EVENT : "+event.toJson().toString(),OnLog.WARN);
    }

    public void onInventory(Inventory inventory, Inventory.Stock stock){
        this.gameContext.log(inventory.type()+" : "+inventory.typeId()+" : "+inventory.constrained(),OnLog.WARN);
        if(inventory.rechargeable()) return;
        this.gameContext.log(stock.header().toString(),OnLog.WARN);
        this.gameContext.log(stock.application().toString(),OnLog.WARN);
        this.gameContext.log(stock.reference().toString(),OnLog.WARN);
        ApplicationPreSetup applicationPreSetup = gameContext.applicationSchema().applicationPreSetup();
        Configurable configurable = applicationPreSetup.load(gameContext.applicationSchema().application("item"),stock.stockId());
        if(configurable==null) return;
        this.gameContext.log(configurable.header().toString(),OnLog.WARN);
        this.gameContext.log(configurable.application().toString(),OnLog.WARN);
        this.gameContext.log(configurable.reference().toString(),OnLog.WARN);
        this.gameContext.log(configurable.toJson().toString(),OnLog.WARN);
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
}
