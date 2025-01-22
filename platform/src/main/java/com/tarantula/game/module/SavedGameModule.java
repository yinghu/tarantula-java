package com.tarantula.game.module;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.service.Content;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.presence.*;

import com.tarantula.game.PlayerSavedGames;
import com.tarantula.game.util.SavedGameDeserializer;
import com.tarantula.platform.presence.saves.CurrentSaveIndex;
import com.tarantula.platform.presence.saves.PlatformSavedGameServiceProvider;
import com.tarantula.platform.presence.saves.SaveRevisionInfo;
import com.tarantula.platform.presence.saves.SavedGame;


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

        else if(session.action().equals("onGetRevision")){
            session.write(savedGameServiceProvider.saveRevisionInfo(session).toBinary());
        }
        else if(session.action().equals("onSetRevision")){
            SaveRevisionInfo saveRevisionInfo = new SaveRevisionInfo();
            JsonObject data = JsonUtil.parse(session.payload());
            saveRevisionInfo.clientRevisionNumber = data.get("RevisionNumber").getAsInt();
            if(data.has("DeviceId")) saveRevisionInfo.deviceId = data.get("DeviceId").getAsString();
            saveRevisionInfo.name(data.get("Name").getAsString());
            session.write(JsonUtil.toSimpleResponse(savedGameServiceProvider.saveRevisionInfo(session,saveRevisionInfo),session.action()).getBytes());
        }

        else if(session.action().equals("onSet")){
            session.write(JsonUtil.toSimpleResponse(true,"data saved ["+session.name()+"]").getBytes());
            this.savedGameServiceProvider.saveData(session,session.name(),bytes);
        }
        else if(session.action().equals("onGet")){
            byte[] data = this.savedGameServiceProvider.loadData(session,session.name());
            session.write(JsonUtil.toSimpleResponse(
                    data != null,
                    data == null ? session.name() : new String(data)//need to use byte array directly down to wire
            ).getBytes());
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
            boolean successful = gameServiceProvider.presenceServiceProvider().createProfile(session);

            session.write(JsonUtil.toSimpleResponse(successful,"create player profile").getBytes());
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
        this.context.log("Saved game module started on tag->"+this.context.descriptor().tag(), OnLog.INFO);
    }

}
