package com.tarantula.game.module;

import com.tarantula.*;
import com.tarantula.Module;
import com.tarantula.game.Stub;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.platform.OnAccessTrack;
import com.tarantula.game.service.Rating;
import com.tarantula.platform.util.SystemUtil;
import java.util.concurrent.ConcurrentHashMap;
/**
 * Created by yinghu lu on 4/14/2020.
 */
public class MatchMakingModule implements Module {

    private ApplicationContext context;
    private ConcurrentHashMap<Integer,Descriptor> mZone = new ConcurrentHashMap<>();
    private GameServiceProvider gameServiceProvider;
    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate update) throws Exception {
        //check Rating to match the game zone to join 
        if(session.action().equals("onPlay")){
            OnAccess access = new OnAccessTrack();
            Rating rating = this.gameServiceProvider.rating(session.systemId());
            access.payload(SystemUtil.toJson(rating.toMap()));
            access.accessMode(Session.FAST_PLAY_MODE);
            context.presence(session.systemId()).onPlay(session,access,mZone.get(rating.rank));

            rating.update(new Stub());
            rating.update();
            context.log(rating.toString(),OnLog.WARN);
            Statistics statistics = this.gameServiceProvider.statistics(session.systemId());
            context.log(statistics.toString(),OnLog.WARN);
            statistics.entry("jc").value(1);
            statistics.entry("kc").value(1);
            statistics.entry("wc").value(1);
            statistics.update();
            context.log(statistics.toString(),OnLog.WARN);

        }
        return false;
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        this.context = context;
        String lb = this.context.descriptor().typeId().replace("service","lobby");
        Lobby lobby = this.context.lobby(lb);
        lobby.entryList().forEach((d)->{
            context.log("Add lobby ->"+d.tag()+" ->rank ["+d.accessRank()+"]",OnLog.WARN);
            mZone.put(d.accessRank(),d);
        });
        String gz = this.context.descriptor().typeId().replace("-service","-data-service");
        this.gameServiceProvider = this.context.serviceProvider(gz);
        this.context.log("Statistics started on game service provider ["+gz+"]", OnLog.WARN);
        context.log("Started match making module on ->"+gz, OnLog.WARN);
    }

    @Override
    public String label() {
        return "matchmaking";
    }
}
