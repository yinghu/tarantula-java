package com.perfectday.games.earth8;


import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.protocol.*;
import com.icodesoftware.service.ApplicationPreSetup;
import com.icodesoftware.util.JsonUtil;

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
        gameContext.applicationSchema().applicationPreSetup().inventoryList(session.distributionId()).forEach(t->{
            t.itemList().forEach(configurable -> {
                //use configurable.distributionId() to check party existing
            });
        });
        //if party check fail return false;
        Transaction transaction = gameContext.applicationSchema().transaction();
        boolean created = transaction.execute(ctx->{
            ApplicationPreSetup applicationPreSetup = (ApplicationPreSetup)ctx;
            DataStore dataStore = applicationPreSetup.onDataStore("battle");
            return dataStore.create(battleTransaction);
        });
        session.write(created?battleTransaction.toJson().toString().getBytes():JsonUtil.toSimpleResponse(false,"failed to create battle transaction").getBytes());
    }
    public void updateGame(Session session,byte[] payload) throws Exception{
        JsonObject jsonObject = JsonUtil.parse(payload);
        //long battleId = jsonObject.get("BattleId").getAsLong();
        //if(battleId<=0) {
            //session.write(JsonUtil.toSimpleResponse(false,"invalid battleId").getBytes());
            //return;
        //}
        //BattleTransaction battleTransaction = new BattleTransaction();
        //battleTransaction.distributionId(battleId);
        //Transaction transaction = gameContext.applicationSchema().transaction();
        //boolean updated = transaction.execute(ctx->{
            //ApplicationPreSetup applicationPreSetup = (ApplicationPreSetup)ctx;
            //DataStore dataStore = applicationPreSetup.onDataStore("battle");
            //if(!dataStore.load(battleTransaction) || battleTransaction.disabled()) return false;
            //TO DO UPDATE ON CURRENT BATTLE
            //return true;
        //});






        //session.write(jsonObject.toString().getBytes());
        this.gameContext.log(jsonObject.toString(),OnLog.WARN);
        session.write(JsonUtil.toSimpleResponse(true,"battle updated").getBytes());
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

    @Override
    public void onLeft(Session session) {
        gameContext.log(" LEAVE : "+session.distributionKey()+" : "+session.stub(),OnLog.WARN);
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
