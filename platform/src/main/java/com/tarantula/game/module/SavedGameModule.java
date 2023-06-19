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
            String[] query = session.name().split("#");
            CurrentSaveIndex savedGame = this.presenceServiceProvider.selectSave(session,query[0],query[1],query[2]);
            session.write(savedGame.toJson().toString().getBytes());
        }
        else if(session.action().equals("onUpdate")){
            SavedGame updated = builder.create().fromJson(new String(bytes),SavedGame.class);
            SavedGame savedGame = this.presenceServiceProvider.loadSavedGame(session.systemId(),session.name());
            if(savedGame!=null){
                savedGame.version=(updated.version);
                savedGame.index(updated.index());
                savedGame.owner(updated.owner());
                savedGame.name(updated.name());
                savedGame.update();
                JsonObject resp = savedGame.toJson();
                resp.addProperty(Response.RESPONSE_SUCCESSFUL,true);
                session.write(resp.toString().getBytes());
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,"no such saved game").getBytes());
            }
        }
        else if(session.action().equals("onReset")){
            SavedGame savedGame = this.presenceServiceProvider.loadSavedGame(session.systemId(),session.name());
            if(savedGame!=null){
                //DailyLoginTrack dailyLoginTrack = gameServiceProvider.dailyGiveawayServiceProvider().checkDailyLogin(savedGame.distributionKey());
                //if(dailyLoginTrack!=null) dailyLoginTrack.reset();
                //AchievementProgress achievementProgress = gameServiceProvider.achievementServiceProvider().achievementProgress(savedGame.distributionKey());
                //if(achievementProgress!=null) achievementProgress.reset();
                savedGame.version=(0);
                savedGame.update();
                JsonObject resp = savedGame.toJson();
                resp.addProperty(Response.RESPONSE_SUCCESSFUL,true);
                session.write(resp.toString().getBytes());
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,"no such saved game").getBytes());
            }
        }
        else if(session.action().equals("onMerge")){
            SavedGame updated = builder.create().fromJson(new String(bytes),SavedGame.class);
            SavedGame current =presenceServiceProvider.loadSavedGame(session.systemId(), session.name());
            SavedGame remote = presenceServiceProvider.loadSavedGame(updated.owner(), updated.distributionKey());
            current.version=(remote.version);
            current.timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
            //current.playerSaveIndex = this.gameServiceProvider.presenceServiceProvider().loadPlayerSaveIndex(remote.owner());
            JsonObject resp = new JsonObject();
            resp.add("_currentSavedGame", current.toJson());
            resp.addProperty(Response.RESPONSE_SUCCESSFUL,true);
            session.write(resp.toString().getBytes());
        }

        else if(session.action().equals("onDailyRewardClaim")){
            //boolean rewarded = this.gameServiceProvider.dailyGiveawayServiceProvider().redeem(session.systemId(),session.name());
            //session.write(JsonUtil.toSimpleResponse(rewarded,session.name()).getBytes());
        }
        else if(session.action().equals("onSet")){
            if(bytes.length > savedGameServiceProvider.mappingObjectMaxSize()){
                session.write(JsonUtil.toSimpleResponse(false,"data size must be less than ["+4000+"]").getBytes());
            }else{
                //String[] query = session.name().split("#");
                //PlayerSaveIndex savedGame = presenceServiceProvider.loadPlayerSaveIndex(session.systemId());
                //if(savedGame.addKey(query[1])) savedGame.update();
                MappingObject mo = new MappingObject();
                //mo.distributionKey(query[0]);
                mo.label(session.name());
                mo.value(bytes);
                //boolean suc = dataStore.update(mo)
                this.savedGameServiceProvider.save(session,mo);
                session.write(JsonUtil.toSimpleResponse(true,session.name()).getBytes());
            }
        }
        else if(session.action().equals("onGet")){
            //String[] query = session.name().split("#");
            //PlayerSaveIndex savedGame = presenceServiceProvider.loadPlayerSaveIndex(session.systemId());
            //if(savedGame.addKey(query[1])) savedGame.update();
            //MappingObject mo = new MappingObject();
            //mo.distributionKey(query[0]);
            //mo.label(query[1]);
            //byte[] v = null;
            //if(dataStore.load(mo)){
                //v = mo.value();
            //}
            session.write(JsonUtil.toSimpleResponse(true,session.name()).getBytes());
        }
        else if(session.action().equals("onDelete")){
            //String[] query = session.name().split("#");
            //PlayerSaveIndex savedGame = presenceServiceProvider.loadPlayerSaveIndex(session.systemId());
            //if(savedGame.addKey(query[1])) savedGame.update();
            //MappingObject mo = new MappingObject();
            //mo.distributionKey(query[0]);
            //mo.label(query[1]);
            //byte[] v = null;
            //if(dataStore.load(mo)){
            //v = mo.value();
            //}
            session.write(JsonUtil.toSimpleResponse(true,session.name()).getBytes());
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
