package com.tarantula.game.module;

import com.icodesoftware.*;
import com.icodesoftware.service.TokenValidatorProvider;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.inbox.Inbox;
import com.tarantula.platform.presence.dailygiveaway.DailyLoginTrack;


public class GameInboxModule extends ModuleHeader{

    private TokenValidatorProvider tokenValidatorProvider;
    @Override
    public boolean onRequest(Session session, byte[] bytes) throws Exception {
        if(session.action().equals("onInbox")){
            Inbox inbox = this.gameServiceProvider.inboxServiceProvider().inbox(session);
            session.write(inbox.toJson().toString().getBytes());
        }
        else if(session.action().equals("onFile")){
            TokenValidatorProvider.AuthVendor download = tokenValidatorProvider.authVendor(OnAccess.DOWNLOAD_CENTER);
            byte[] payload = download.download(gameServiceProvider.gameCluster().typeId()+"#"+session.name());
            session.write(payload);
        }
        else if(session.action().equals("onRedeem")){
            boolean suc = this.gameServiceProvider.inboxServiceProvider().redeem(session,session.name());
            session.write(JsonUtil.toSimpleResponse(suc,session.name()).getBytes());
        }
        else if(session.action().equals("onInventory")){
            Inventory inventory = this.gameServiceProvider.inventoryServiceProvider().inventory(session);
            if(inventory==null) {
                session.write(JsonUtil.toSimpleResponse(false,"Inventory ["+session.name()+"] no existed").getBytes());
            }else{
                session.write(inventory.toJson().toString().getBytes());
            }
        }
        else if(session.action().equals("onStatistics")){
            Statistics statistics = this.gameServiceProvider.presenceServiceProvider().statistics(session);
            session.write(statistics.toJson().toString().getBytes());
        }
        else if(session.action().equals("onRating")){
            Rating rating = this.gameServiceProvider.presenceServiceProvider().rating(session);
            session.write(rating.toJson().toString().getBytes());
        }
        else if(session.action().equals("onDailyReward")){
            DailyLoginTrack dailyLoginTrack = this.gameServiceProvider.dailyGiveawayServiceProvider().claim(session);
            if(dailyLoginTrack!=null){
                session.write(dailyLoginTrack.toJson().toString().getBytes());
            }else{
                session.write(JsonUtil.toSimpleResponse(false,"no daily login reward").getBytes());
            }
        }
        else if(session.action().equals("onAchievement")){
            Achievement achievement = this.gameServiceProvider.achievementServiceProvider().achievement(session);
            achievement.onProgress(Double.parseDouble(session.name()));
            session.write(achievement.toJson().toString().getBytes());
        }
        else if(session.action().equals("onLeaderBoard")) {
            LeaderBoard leaderBoard = this.gameServiceProvider.leaderBoardProvider().leaderBoard(session.name());
            session.write(leaderBoard.toJson().toString().getBytes());
        }
        else if(session.action().equals("onMailbox")){
            session.write(this.gameServiceProvider.inboxServiceProvider().mailbox(session).toJson().toString().getBytes());
        }
        else if(session.action().equals("onPlayerEventCompleted")){
            gameServiceProvider.gameServiceProvider().updateGame(session,null);
            session.write(JsonUtil.toSimpleResponse(true, "Player event " + session.name() + " completed").getBytes());
        }
        else if(session.action().equals("onCheckGlobalItemGrants")){
            this.gameServiceProvider.inboxServiceProvider().checkGlobalItemGrant(session, gameServiceProvider.gameCluster().distributionId());
            session.write(JsonUtil.toSimpleResponse(true, "Done").getBytes());
        }
        else{
            throw new UnsupportedOperationException(session.action()+" not support");
        }

        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        super.setup(applicationContext);
        tokenValidatorProvider = applicationContext.serviceProvider(TokenValidatorProvider.NAME);
        this.context.log("Game inbox module started -"+this.context.descriptor().tag(), OnLog.WARN);
    }

}
