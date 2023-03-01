package com.tarantula.game.module;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.icodesoftware.Module;
import com.icodesoftware.*;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.game.service.GameServiceProvider;

import com.tarantula.game.PlayerSavedGames;
import com.tarantula.game.util.SavedGameDeserializer;
import com.tarantula.platform.achievement.AchievementProgress;
import com.tarantula.platform.presence.DailyLoginTrack;
import com.tarantula.platform.presence.saves.SavedGame;


import java.time.LocalDateTime;

public class SavedGameModule implements Module {
    private ApplicationContext context;
    private GameServiceProvider gameServiceProvider;
    private GsonBuilder builder;
    @Override
    public boolean onRequest(Session session, byte[] bytes) throws Exception {
        if(session.action().equals("onList")) {
            String[] query = session.name().split("#");
            PlayerSavedGames playerSavedGames = new PlayerSavedGames(session.systemId(),query[0],this.gameServiceProvider.presenceServiceProvider().listSaves(session.systemId(),query[0],query[1]));
            playerSavedGames.gameServiceProvider = gameServiceProvider;
            session.write(playerSavedGames.toJson().toString().getBytes());
        }
        else if(session.action().equals("onUpdate")){
            SavedGame updated = builder.create().fromJson(new String(bytes),SavedGame.class);
            SavedGame savedGame = this.gameServiceProvider.presenceServiceProvider().loadSavedGame(session.systemId(),session.name());
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
            SavedGame savedGame = this.gameServiceProvider.presenceServiceProvider().loadSavedGame(session.systemId(),session.name());
            if(savedGame!=null){
                DailyLoginTrack dailyLoginTrack = gameServiceProvider.presenceServiceProvider().checkDailyLogin(savedGame.distributionKey());
                if(dailyLoginTrack!=null) dailyLoginTrack.reset();
                AchievementProgress achievementProgress = gameServiceProvider.achievementServiceProvider().achievementProgress(savedGame.distributionKey());
                if(achievementProgress!=null) achievementProgress.reset();
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
            SavedGame current = gameServiceProvider.presenceServiceProvider().loadSavedGame(session.systemId(), session.name());
            SavedGame remote = gameServiceProvider.presenceServiceProvider().loadSavedGame(updated.owner(), updated.distributionKey());
            current.version=(remote.version);
            current.timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
            current.playerSaveIndex = this.gameServiceProvider.presenceServiceProvider().loadPlayerSaveIndex(remote.owner());
            JsonObject resp = new JsonObject();
            resp.add("_currentSavedGame", current.toJson());
            resp.addProperty(Response.RESPONSE_SUCCESSFUL,true);
            session.write(resp.toString().getBytes());
        }

        else if(session.action().equals("onDailyRewardClaim")){
            boolean rewarded = this.gameServiceProvider.presenceServiceProvider().redeem(session.systemId(),session.name());
            session.write(JsonUtil.toSimpleResponse(rewarded,session.name()).getBytes());
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
        this.gameServiceProvider = this.context.serviceProvider(context.descriptor().typeId());
        this.gameServiceProvider.exportServiceModule(this.context.descriptor().tag(),this);
        this.context.log("Saved game module started on tag->"+this.context.descriptor().tag(), OnLog.WARN);
    }
}
