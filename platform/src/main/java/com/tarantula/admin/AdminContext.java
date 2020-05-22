package com.tarantula.admin;

import com.tarantula.Access;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.presence.GameCluster;

import java.util.List;

public class AdminContext extends ResponseHeader {
    public List<GameCluster> gameClusterList;
    public List<Access> userList;

}
