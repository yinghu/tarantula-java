package com.tarantula.platform.service.cluster;

import com.tarantula.platform.service.MemberDiscovery;

public interface ScopedMemberDiscovery extends MemberDiscovery {

    void scope(int scope);
}
