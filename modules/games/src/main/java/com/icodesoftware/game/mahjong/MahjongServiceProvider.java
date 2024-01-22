package com.icodesoftware.game.mahjong;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.game.Dice;
import com.icodesoftware.protocol.*;
import com.icodesoftware.service.ApplicationPreSetup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MahjongServiceProvider implements GameServiceProvider {


    private GameContext gameContext;

    private Stack stack;
    private Dice dice;

    @Override
    public void onInventory(ApplicationPreSetup applicationPreSetup, Inventory inventory, Inventory.Stock inventoryItem) {

    }

    @Override
    public void tournamentStarted(Tournament tournament) {

    }

    @Override
    public void tournamentClosed(Tournament tournament) {

    }

    @Override
    public void tournamentEnded(Tournament tournament) {

    }

    @Override
    public void onValidated(Channel channel) {
        this.gameContext.log("on validated",OnLog.INFO);
    }

    @Override
    public void onJoined(Channel channel) {
        this.gameContext.log("on joined channel",OnLog.INFO);
    }

    @Override
    public void onLeft(Channel channel) {
        this.gameContext.log("on left channel",OnLog.INFO);
    }

    @Override
    public void setup(GameContext gameContext) {
        this.gameContext = gameContext;
        this.gameContext.log("Mahjong service provider started",OnLog.INFO);
        this.stack = Stack.stack(3);
        this.dice = Dice.dice(2);
    }

    @Override
    public void onJoined(Session session, Room room) {
        this.gameContext.log("Mahjong service on joined",OnLog.INFO);
    }

    @Override
    public void startGame(Session session, byte[] payload) throws Exception {

    }

    @Override
    public void updateGame(Session session, byte[] payload) throws Exception {

    }

    @Override
    public void endGame(Session session, byte[] payload) throws Exception {

    }

    @Override
    public <T extends OnAccess> void onGameEvent(T event) {

    }

    @Override
    public void onLeft(Session session) {
        this.gameContext.log("Mahjong service on left",OnLog.INFO);
    }

    @Override
    public byte[] onRequest(Session session, MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        this.gameContext.log("Mahjong service on request : "+session.systemId()+" : "+session.stub(),OnLog.INFO);
        if(messageHeader.commandId == Messenger.REQUEST){
            short cmd = messageBuffer.readShort();
            String name = messageBuffer.readUTF8();
            float value = messageBuffer.readFloat();
            this.gameContext.log("Request : "+cmd+" : "+name+" : "+value,OnLog.INFO);
        }
        JsonObject jsonObject = new JsonObject();
        JsonArray data = new JsonArray();
        for(int i=0;i<100;i++){
            JsonObject e = new JsonObject();
            e.addProperty("e"+i,i);
            data.add(e);
        }
        jsonObject.add("stats",data);
        return jsonObject.toString().getBytes();
    }

    @Override
    public void onAction(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer, UDPEndpointServiceProvider.RelayListener callback) {
        if(messageHeader.commandId == Messenger.ACTION){
            short cmd = messageBuffer.readShort();
            switch (cmd){
                case ClassicMahjong.SHUFFLE:
                    handleShuffle(messageHeader,messageBuffer,callback);
                    break;
                case ClassicMahjong.START:
                    handleStart(messageHeader,messageBuffer,callback);
                    break;
                case ClassicMahjong.SWAP:
                    handle3(messageHeader,messageBuffer,callback);
                    break;
                default:
                    this.gameContext.log("Command ["+cmd+"] not supported",OnLog.WARN);
            }
        }
    }

    private void handleShuffle(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer, UDPEndpointServiceProvider.RelayListener callback){
        float value = messageBuffer.readFloat();
        String name = messageBuffer.readUTF8();
        this.gameContext.log("Action : "+name+" : "+value,OnLog.INFO);
        int[] cutter = dice.roll();
        stack.shuffle(cutter[0]+cutter[1]);
        messageHeader.commandId = 1001;
        messageHeader.ack = true;
        messageHeader.encrypted = false;
        messageHeader.broadcasting = true;
        messageBuffer.reset().writeHeader(messageHeader).writeUTF8("RSP : "+name);
        callback.onRelay(messageHeader,messageBuffer.flip());
    }

    private void handleStart(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer, UDPEndpointServiceProvider.RelayListener callback){
        float value = messageBuffer.readFloat();
        String name = messageBuffer.readUTF8();
        this.gameContext.log("Action : "+name+" : "+value,OnLog.INFO);
        Tile[] hand = new Tile[14];
        if(!stack.draw(hand)){
            messageHeader.commandId = 1000;
            messageHeader.ack = true;
            messageHeader.encrypted = false;
            messageHeader.broadcasting = true;
            messageBuffer.reset().writeHeader(messageHeader).writeUTF8("RSP : "+name);
            callback.onRelay(messageHeader,messageBuffer.flip());
            return;
        }
        messageHeader.commandId = 1002;
        messageHeader.ack = true;
        messageHeader.encrypted = false;
        messageHeader.broadcasting = true;
        messageBuffer.reset();
        messageBuffer.writeHeader(messageHeader);
        messageBuffer.writeInt(hand.length);
        Arrays.sort(hand,new TitleComparator());
        for(Tile tile : hand){
            messageBuffer.writeInt(tile.rank).writeUTF8(tile.name);
        }
        callback.onRelay(messageHeader,messageBuffer.flip());
    }
    private void handle3(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer, UDPEndpointServiceProvider.RelayListener callback){
        float value = messageBuffer.readFloat();
        String name = messageBuffer.readUTF8();
        this.gameContext.log("Action : "+name+" : "+value,OnLog.INFO);
        //Tile[] hand = new Tile[3];
        //stack.draw()
        messageHeader.commandId = 1003;
        messageHeader.ack = true;
        messageHeader.encrypted = false;
        messageHeader.broadcasting = true;
        messageBuffer.reset().writeHeader(messageHeader).writeUTF8("RSP : "+name);
        callback.onRelay(messageHeader,messageBuffer.flip());
    }


    public <T extends OnAccess> List<T> inbox(Session session){
        return new ArrayList<>();
    }
}
