package com.tarantula.game.module;

import com.google.gson.GsonBuilder;
import com.icodesoftware.*;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.presence.*;

import com.tarantula.game.PlayerSavedGames;
import com.tarantula.game.util.SavedGameDeserializer;
import com.tarantula.platform.presence.saves.CurrentSaveIndex;
import com.tarantula.platform.presence.saves.PlatformSavedGameServiceProvider;
import com.tarantula.platform.presence.saves.SavedGame;

import java.util.ArrayList;
import java.util.List;

public class SavedGameModule extends ModuleHeader {

    private PlatformSavedGameServiceProvider savedGameServiceProvider;
    private PlatformPresenceServiceProvider presenceServiceProvider;
    private GsonBuilder builder;

    @Override
    public boolean onRequest(Session session, byte[] bytes) throws Exception {
        if(session.action().equals("onList")) {
            PlayerSavedGames playerSavedGames = new PlayerSavedGames(session.systemId(),this.savedGameServiceProvider.savedGameList(session));
            session.write(playerSavedGames.toJson().toString().getBytes());
        }
        else if(session.action().equals("onSelect")){
            CurrentSaveIndex savedGame = this.savedGameServiceProvider.selectSavedGame(session);
            session.write(savedGame!=null?savedGame.toJson().toString().getBytes():JsonUtil.toSimpleResponse(false,"save in use").getBytes());
        }
        else if(session.action().equals("onSet")){
            session.write(JsonUtil.toSimpleResponse(true,"data saved ["+session.name()+"]").getBytes());
            this.savedGameServiceProvider.saveData(session,session.name(),bytes);
        }
        else if(session.action().equals("onGet")){
            byte[] data = this.savedGameServiceProvider.loadData(session,session.name());
            session.write(data!=null? data : JsonUtil.toSimpleResponse(false,session.name()).getBytes());
        }
        else if(session.action().equals("onReset")){
            CurrentSaveIndex selected = this.savedGameServiceProvider.reset(session);
            if(selected.index()!=null){
                SavedGame savedGame = presenceServiceProvider.resetSavedGame(selected);
                session.write(savedGame.toJson().toString().getBytes());
            }
            else{
                session.write(JsonUtil.toSimpleResponse(true,"system saved game reset").getBytes());
            }
        }
        else if(session.action().equals("onFetchProfile")){
            session.write(presenceServiceProvider.getProfilePayload(session.name()).toJson().toString().getBytes());
        }
        else if(session.action().equals("onUpdateProfile")){
            boolean sucessfull = gameServiceProvider.presenceServiceProvider().createProfile(session);

            session.write(JsonUtil.toSimpleResponse(sucessfull,"create player profile").getBytes());
        }
        else{
            throw new UnsupportedOperationException(session.action());
        }
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        super.setup(applicationContext);
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(SavedGame.class,new SavedGameDeserializer());
        this.savedGameServiceProvider = gameServiceProvider.savedGameServiceProvider();
        this.presenceServiceProvider = gameServiceProvider.presenceServiceProvider();
        this.context.log("Saved game module started on tag->"+this.context.descriptor().tag(), OnLog.WARN);
    }

}
