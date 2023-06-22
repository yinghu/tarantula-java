package com.tarantula.game.module;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.icodesoftware.Module;
import com.icodesoftware.*;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.game.MappingObject;
import com.tarantula.game.service.PlatformGameServiceProvider;

import com.tarantula.game.PlayerSavedGames;
import com.tarantula.game.util.SavedGameDeserializer;
import com.tarantula.platform.presence.PlatformPresenceServiceProvider;
import com.tarantula.platform.presence.saves.CurrentSaveIndex;
import com.tarantula.platform.presence.saves.PlatformSavedGameServiceProvider;
import com.tarantula.platform.presence.saves.PlayerSaveIndex;
import com.tarantula.platform.presence.saves.SavedGame;


import java.time.LocalDateTime;

public class SavedGameModule implements Module {
    private ApplicationContext context;
    private PlatformSavedGameServiceProvider savedGameServiceProvider;
    private PlatformPresenceServiceProvider presenceServiceProvider;
    private GsonBuilder builder;

    @Override
    public boolean onRequest(Session session, byte[] bytes) throws Exception {
        if(session.action().equals("onList")) {
            PlayerSavedGames playerSavedGames = new PlayerSavedGames(session.systemId(),this.presenceServiceProvider.listSaves(session.systemId(),session.name()));
            session.write(playerSavedGames.toJson().toString().getBytes());
        }
        else if(session.action().equals("onSelect")){
            CurrentSaveIndex savedGame = this.presenceServiceProvider.selectSave(session,session.name());
            session.write(savedGame!=null?savedGame.toJson().toString().getBytes():JsonUtil.toSimpleResponse(false,"save in use").getBytes());
        }
        else if(session.action().equals("onSet")){
            if(bytes.length > savedGameServiceProvider.mappingObjectMaxSize()){
                session.write(JsonUtil.toSimpleResponse(false,"data size must be less than ["+4000+"]").getBytes());
            }else{
                MappingObject mo = new MappingObject();
                mo.label(session.name());
                savedGameServiceProvider.load(session,mo);
                mo.value(bytes);
                this.savedGameServiceProvider.save(session,mo);
                session.write(JsonUtil.toSimpleResponse(true,mo.key().asString()).getBytes());
            }
        }
        else if(session.action().equals("onGet")){
            MappingObject mo = new MappingObject();
            mo.label(session.name());
            boolean suc = savedGameServiceProvider.load(session,mo);
            session.write(suc ? mo.value() : JsonUtil.toSimpleResponse(false,session.name()).getBytes());
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
        else{
            throw new UnsupportedOperationException(session.action());
        }
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(SavedGame.class,new SavedGameDeserializer());
        PlatformGameServiceProvider gameServiceProvider = this.context.serviceProvider(context.descriptor().typeId());
        gameServiceProvider.exportServiceModule(this.context.descriptor().tag(),this);
        this.savedGameServiceProvider = gameServiceProvider.savedGameServiceProvider();
        this.presenceServiceProvider = gameServiceProvider.presenceServiceProvider();
        this.context.log("Saved game module started on tag->"+this.context.descriptor().tag(), OnLog.WARN);
    }
}
