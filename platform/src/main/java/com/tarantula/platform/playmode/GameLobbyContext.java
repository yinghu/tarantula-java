package com.tarantula.platform.playmode;

import com.tarantula.InstanceRegistry;
import com.tarantula.platform.ResponseHeader;

import java.util.List;

public class GameLobbyContext extends ResponseHeader {

    public GameLobbyContext(String cmd){
        this.command = cmd;
        this.successful = true;
    }

    public List<GameDescriptor> gameList;
    public List<InstanceRegistry> onList;
}
