package com.tarantula.game.module;

import com.google.gson.JsonObject;
import com.icodesoftware.Module;
import com.icodesoftware.*;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.service.GameServiceProvider;

import com.tarantula.game.PlayerSavedGames;
import com.tarantula.platform.presence.saves.SavedGame;

public class SavedGameModule implements Module {
    private ApplicationContext context;
    private GameServiceProvider gameServiceProvider;
    @Override
    public boolean onRequest(Session session, byte[] bytes) throws Exception {
        if(session.action().equals("onList")) {
            String[] query = session.name().split("#");
            PlayerSavedGames playerSavedGames = new PlayerSavedGames(session.systemId(),query[0],this.gameServiceProvider.presenceServiceProvider().listSaves(session.systemId(),query[0],query[1]));
            playerSavedGames.gameServiceProvider = gameServiceProvider;
            session.write(playerSavedGames.toJson().toString().getBytes());
        }
        else if(session.action().equals("onUpdate")){
            SavedGame savedGame = this.gameServiceProvider.presenceServiceProvider().loadSavedGame(session.systemId(),session.name());
            if(savedGame!=null){
                savedGame.version(savedGame.version()+1);
                savedGame.update();
                JsonObject resp = savedGame.toJson();
                resp.addProperty(Response.RESPONSE_SUCCESSFUL,true);
                session.write(resp.toString().getBytes());
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,"no such saved game").getBytes());
            }
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
        this.gameServiceProvider = this.context.serviceProvider(context.descriptor().typeId());
        this.context.log("Saved game module started on tag->"+this.context.descriptor().tag(), OnLog.WARN);
    }
}
