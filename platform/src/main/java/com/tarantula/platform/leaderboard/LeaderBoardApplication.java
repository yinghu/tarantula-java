package com.tarantula.platform.leaderboard;

import com.tarantula.*;
import com.tarantula.platform.TarantulaApplicationHeader;
import com.tarantula.platform.service.LeaderBoardServiceProvider;
import com.tarantula.platform.util.LeaderBoardSerializer;


/**
 * Updated 9/2/19yinghu lu
 */
public class LeaderBoardApplication extends TarantulaApplicationHeader{


    private LeaderBoardServiceProvider leaderBoardServiceProvider;
    @Override
    public void callback(Session session, byte[] payload) throws Exception {
        OnAccess cmd = this.builder.create().fromJson(new String(payload).trim(), OnAccess.class);
        if(session.action().equals("onLeaderBoard")){// query header, name and classifier eg Presence, Top10, LoginCount ,Total
           LeaderBoard ldx = leaderBoardServiceProvider.leaderBoard(cmd.property("header"),cmd.property("category"),cmd.property("classifier"));
           session.write(this.builder.create().toJson(ldx).getBytes(),this.descriptor.responseLabel());
        }
        else{
            throw new RuntimeException("action ["+session.action()+"] not supported");
        }

    }
    public void setup(ApplicationContext context) throws Exception {
        super.setup(context);
        builder.registerTypeAdapter(TopListLeaderBoard.class,new LeaderBoardSerializer());
        leaderBoardServiceProvider = context.serviceProvider(this.context.configuration("setup").property("name"));
        this.context.log("Leader board application started on ["+descriptor.tag()+"]",OnLog.INFO);
    }
}
