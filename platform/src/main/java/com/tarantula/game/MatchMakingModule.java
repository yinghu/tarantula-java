package com.tarantula.game;

import com.tarantula.*;
import com.tarantula.Module;
import com.tarantula.platform.OnAccessTrack;
import com.tarantula.platform.util.SystemUtil;

import java.util.concurrent.ConcurrentHashMap;
/**
 * Created by yinghu lu on 4/14/2020.
 */
public class MatchMakingModule implements Module {

    private ApplicationContext context;
    private ConcurrentHashMap<Integer,Descriptor> mZone = new ConcurrentHashMap<>();
    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate update) throws Exception {
        //check Rating to match the game zone to join 
        if(session.action().equals("onPlay")){
            OnAccess access = new OnAccessTrack();
            Rating rating = new Rating();
            access.payload(SystemUtil.toJson(rating.toMap()));
            access.accessMode(Session.FAST_PLAY_MODE);
            context.presence(session.systemId()).onPlay(session,access,mZone.get(1));
        }
        return false;
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        this.context = context;
        Lobby lobby = this.context.lobby("game-zone");
        lobby.entryList().forEach((d)->{
            context.log("Add zone ->"+d.tag()+" ->rank ["+d.accessRank()+"]",OnLog.WARN);
            mZone.put(d.accessRank(),d);
        });
        context.log("Started match making module", OnLog.WARN);
    }

    @Override
    public String label() {
        return "matchmaking";
    }
}
