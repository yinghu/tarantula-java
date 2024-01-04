package com.icodesoftware.game.mahjong;

import com.icodesoftware.*;
import com.icodesoftware.protocol.*;
import com.icodesoftware.service.ApplicationPreSetup;

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

    }

    @Override
    public void onJoined(Channel channel) {

    }

    @Override
    public void onLeft(Channel channel) {

    }

    @Override
    public void setup(GameContext gameContext) {
        this.gameContext = gameContext;
        this.gameContext.log("Mahjong service provider started",OnLog.INFO);
    }

    @Override
    public void onJoined(Session session, Room room) {

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

    }

    @Override
    public byte[] onRequest(Session session, MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        return new byte[0];
    }

    @Override
    public void onAction(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer, UDPEndpointServiceProvider.RelayListener callback) {

    }
}
