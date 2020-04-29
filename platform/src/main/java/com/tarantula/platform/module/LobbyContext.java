package com.tarantula.platform.module;

import com.tarantula.Descriptor;
import com.tarantula.InstanceRegistry;
import com.tarantula.platform.ResponseHeader;

import java.util.List;

public class LobbyContext extends ResponseHeader {

    public LobbyContext(String cmd){
        this.command = cmd;
        this.successful = true;
    }

    public List<Descriptor> gameList;
    public List<InstanceRegistry> onList;
}
