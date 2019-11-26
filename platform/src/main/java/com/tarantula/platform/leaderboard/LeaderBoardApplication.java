package com.tarantula.platform.leaderboard;

import com.tarantula.*;
import com.tarantula.platform.TarantulaApplicationHeader;
import com.tarantula.platform.presence.PresenceContext;
import com.tarantula.platform.service.LeaderBoardServiceProvider;
import com.tarantula.platform.util.PresenceContextSerializer;


/**
 * Updated 9/2/19 yinghu lu
 */
public class LeaderBoardApplication extends TarantulaApplicationHeader{


    private LeaderBoardServiceProvider leaderBoardServiceProvider;
    @Override
    public void callback(Session session, byte[] payload) throws Exception {
        OnAccess cmd = this.builder.create().fromJson(new String(payload).trim(), OnAccess.class);
        if(session.action().equals("onLeaderBoard")){// query header, name and classifier eg Presence, Top10, LoginCount ,Total
            PresenceContext presenceContext = new PresenceContext();
            LeaderBoard ldx = leaderBoardServiceProvider.leaderBoard(cmd.property("header"),cmd.property("category"),cmd.property("classifier"));
            if(ldx!=null){
                presenceContext.leaderBoard = ldx;
            }
            else{
                presenceContext.successful(false);
                presenceContext.message("leader board not available");
            }
            session.write(this.builder.create().toJson(presenceContext).getBytes(),this.descriptor.responseLabel());
        }
        else{
            throw new RuntimeException("action ["+session.action()+"] not supported");
        }

    }
    public void setup(ApplicationContext context) throws Exception {
        super.setup(context);
        builder.registerTypeAdapter(PresenceContext.class,new PresenceContextSerializer());
        leaderBoardServiceProvider = context.serviceProvider(this.context.configuration("setup").property("name"));
        this.context.log("Leader board application started on ["+descriptor.tag()+"]",OnLog.INFO);
    }
}
