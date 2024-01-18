package com.icodesoftware.game.mahjong;

import com.icodesoftware.*;
import com.icodesoftware.protocol.*;
import com.icodesoftware.service.ApplicationPreSetup;

import java.util.ArrayList;
import java.util.List;

public class MahjongServiceProvider implements GameServiceProvider {


    private GameContext gameContext;
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
        return new byte[0];
    }

    @Override
    public void onAction(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer, UDPEndpointServiceProvider.RelayListener callback) {
        this.gameContext.log("Mahjong service on action",OnLog.INFO);
        if(messageHeader.commandId == Messenger.ACTION){
            short cmd = messageBuffer.readShort();
            float value = messageBuffer.readFloat();
            String name = messageBuffer.readUTF8();
            this.gameContext.log("Action : "+cmd+" : "+name+" : "+value,OnLog.INFO);
        }
    }

    public <T extends OnAccess> List<T> inbox(Session session){
        return new ArrayList<>();
    }
}
