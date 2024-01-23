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

    private Tile[] hand;

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
                case ClassicMahjong.DRAW:
                    handleDraw(messageHeader,messageBuffer,callback);
                    break;
                case ClassicMahjong.SWAP:
                    handleSwap(messageHeader,messageBuffer,callback);
                    break;
                case ClassicMahjong.CLAIM:
                    handleClaim(messageHeader,messageBuffer,callback);
                    break;
                default:
                    this.gameContext.log("Command ["+cmd+"] not supported",OnLog.WARN);
            }
        }
    }

    private void handleShuffle(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer, UDPEndpointServiceProvider.RelayListener callback){
        int[] cutter = dice.roll();
        stack.shuffle(cutter[0]+cutter[1]);
        messageHeader.commandId = 1001;
        messageHeader.ack = true;
        messageHeader.encrypted = false;
        messageHeader.broadcasting = true;
        int[] debug = stack.debug();
        messageBuffer.reset().writeHeader(messageHeader);
        messageBuffer.writeInt(debug.length);
        for(int i=0;i<debug.length;i++){
            messageBuffer.writeInt(debug[i]);
        }
        callback.onRelay(messageHeader,messageBuffer.flip());
    }

    private void handleStart(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer, UDPEndpointServiceProvider.RelayListener callback){
        int value = messageBuffer.readInt();
        this.gameContext.log("Start hand : "+value+" : "+value,OnLog.INFO);
        hand = new Tile[value];
        if(!stack.draw(hand)){
            messageHeader.commandId = 1000;
            messageHeader.ack = true;
            messageHeader.encrypted = false;
            messageHeader.broadcasting = true;
            messageBuffer.reset().writeHeader(messageHeader).writeUTF8("Stack overflow");
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
            messageBuffer.writeInt(tile.rank).writeBoolean(tile.swappable).writeUTF8(tile.name);
        }
        callback.onRelay(messageHeader,messageBuffer.flip());
    }
    private void handleDraw(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer, UDPEndpointServiceProvider.RelayListener callback){
        int value = messageBuffer.readInt();
        Tile drop = ClassicMahjong.TILE_MAP.get(value);
        if(ClassicMahjong.fourKind(drop,hand)){
            Tile swap = stack.swap(new Tile[]{drop,drop,drop,drop});
            this.gameContext.log("Swap tile * 4  : "+drop.toString()+" : "+value,OnLog.INFO);
            for(int i=0;i<hand.length;i++){
                if(hand[i].rank == value){
                    hand[i]= swap;
                    break;
                }
            }
            messageHeader.commandId = 1004;
            messageHeader.ack = true;
            messageHeader.encrypted = false;
            messageHeader.broadcasting = true;
            messageBuffer.reset();
            messageBuffer.writeHeader(messageHeader);
            messageBuffer.writeInt(hand.length);
            Arrays.sort(hand,new TitleComparator());
            for(Tile tile : hand){
                messageBuffer.writeInt(tile.rank).writeBoolean(tile.swappable).writeUTF8(tile.name);
            }
            callback.onRelay(messageHeader,messageBuffer.flip());
            return;
        }
        this.gameContext.log("Drop tile : "+drop.toString()+" : "+value,OnLog.INFO);
        Tile[] draw = new Tile[]{drop};
        if(!stack.draw(draw)){
            messageHeader.commandId = 1000;
            messageHeader.ack = true;
            messageHeader.encrypted = false;
            messageHeader.broadcasting = true;
            messageBuffer.reset().writeHeader(messageHeader).writeUTF8("Stack overflow");
            callback.onRelay(messageHeader,messageBuffer.flip());
            return;
        }
        for(int i=0;i<hand.length;i++){
            if(hand[i].rank == value){
                hand[i]=draw[0];
                break;
            }
        }
        messageHeader.commandId = 1003;
        messageHeader.ack = true;
        messageHeader.encrypted = false;
        messageHeader.broadcasting = true;
        messageBuffer.reset();
        messageBuffer.writeHeader(messageHeader);
        messageBuffer.writeInt(hand.length);
        Arrays.sort(hand,new TitleComparator());
        for(Tile tile : hand){
            messageBuffer.writeInt(tile.rank).writeBoolean(tile.swappable).writeUTF8(tile.name);
        }
        callback.onRelay(messageHeader,messageBuffer.flip());
    }
    private void handleSwap(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer, UDPEndpointServiceProvider.RelayListener callback){
        int value = messageBuffer.readInt();
        Tile drop = ClassicMahjong.TILE_MAP.get(value);
        this.gameContext.log("Swap tile : "+drop.toString()+" : "+value,OnLog.INFO);
        if(!drop.swappable){
            messageHeader.commandId = 1000;
            messageHeader.ack = true;
            messageHeader.encrypted = false;
            messageHeader.broadcasting = true;
            messageBuffer.reset().writeHeader(messageHeader).writeUTF8("tile is not swappable overflow");
            callback.onRelay(messageHeader,messageBuffer.flip());
            return;
        }
        Tile swap = stack.swap(drop);
        for(int i=0;i<hand.length;i++){
            if(hand[i].rank == value){
                hand[i]= swap;
                break;
            }
        }
        messageHeader.commandId = 1004;
        messageHeader.ack = true;
        messageHeader.encrypted = false;
        messageHeader.broadcasting = true;
        messageBuffer.reset();
        messageBuffer.writeHeader(messageHeader);
        messageBuffer.writeInt(hand.length);
        Arrays.sort(hand,new TitleComparator());
        for(Tile tile : hand){
            messageBuffer.writeInt(tile.rank).writeBoolean(tile.swappable).writeUTF8(tile.name);
        }
        callback.onRelay(messageHeader,messageBuffer.flip());
    }

    private void handleClaim(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer, UDPEndpointServiceProvider.RelayListener callback){
        Arrays.sort(hand,new TitleComparator());
        if(!ClassicMahjong.claim(hand)){
            messageHeader.commandId = 1000;
            messageHeader.ack = true;
            messageHeader.encrypted = false;
            messageHeader.broadcasting = true;
            messageBuffer.reset().writeHeader(messageHeader).writeUTF8("hand is not done yet");
            callback.onRelay(messageHeader,messageBuffer.flip());
            return;
        }
        messageHeader.commandId = 1005;
        messageHeader.ack = true;
        messageHeader.encrypted = false;
        messageHeader.broadcasting = true;
        messageBuffer.reset().writeHeader(messageHeader).writeUTF8("hand is claimed");
        callback.onRelay(messageHeader,messageBuffer.flip());

    }


    public <T extends OnAccess> List<T> inbox(Session session){
        return new ArrayList<>();
    }
}
