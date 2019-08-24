package com.tarantula.platform.leaderboard;

import com.tarantula.*;
import com.tarantula.platform.TarantulaApplicationHeader;

import com.tarantula.platform.util.LeaderBoardRegistryContextSerializer;
import com.tarantula.platform.util.LeaderBoardSerializer;


/**
 * Updated 4/24/2018 yinghu lu
 */
public class LeaderBoardApplication extends TarantulaApplicationHeader{


    private LeaderBoardServiceProvider leaderBoardServiceProvider;
    @Override
    public void callback(Session session, byte[] payload) throws Exception {
        OnAccess cmd = this.builder.create().fromJson(new String(payload).trim(), OnAccess.class);
        if(session.action().equals("onLeaderBoard")){// query header, name and classifier eg Presence, Top10, LoginCount ,Total
           LeaderBoard ldx = leaderBoardServiceProvider.leaderBoard(cmd.header("header"),cmd.header("category"),cmd.header("classifier"));
           session.write(this.builder.create().toJson(ldx).getBytes(),this.descriptor.responseLabel());
        }
        else if(session.action().equals("onRegistry")){
            LeaderBoardRegistryContext _cbr = new LeaderBoardRegistryContext();
            //_cbr.registryList = leaderBoardServiceProvider.onRegistry();
            session.write(this.builder.create().toJson(_cbr).getBytes(),this.descriptor.responseLabel());
        }

    }
    public void setup(ApplicationContext context) throws Exception {
        super.setup(context);
        builder.registerTypeAdapter(Top10LeaderBoard.class,new LeaderBoardSerializer());
        builder.registerTypeAdapter(LeaderBoardRegistryContext.class,new LeaderBoardRegistryContextSerializer());
        leaderBoardServiceProvider = context.serviceProvider("TOP10");
        this.context.log("Leader board application started on ["+descriptor.tag()+"]",OnLog.INFO);
    }
}
