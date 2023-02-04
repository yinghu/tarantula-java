package com.tarantula.game.service;


import com.icodesoftware.*;
import com.icodesoftware.protocol.GameServiceProxy;
import com.icodesoftware.protocol.MessageBuffer;
import com.tarantula.game.GameLobby;
import com.tarantula.game.GameZone;
import com.tarantula.game.Stub;


abstract public class RoomProxyHeader implements GameZone.RoomProxy {

    protected ApplicationContext context;
    protected GameServiceProvider gameServiceProvider;
    protected Descriptor application;
    protected GameLobby gameLobby;
    protected GameZone gameZone;
    protected DataStore dataStore;

    @Override
    public void setup(ApplicationContext applicationContext, GameLobby gameLobby,GameZone gameZone) {
        this.context = applicationContext;
        this.application = applicationContext.descriptor();
        this.gameServiceProvider = applicationContext.serviceProvider(application.typeId().replace("lobby","service"));
        this.gameLobby = gameLobby;
        this.gameZone = gameZone;
        this.dataStore = gameZone.dataStore();
    }

    public void close(){

    }

    public byte[] onService(Stub stub,MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer){
        short cmd = messageBuffer.readShort();
        GameServiceProxy messageListener = gameLobby.gameServiceProxy(cmd);
        return messageListener.onService(stub,messageHeader,messageBuffer);
    }

}
