package com.tarantula.platform.service;

import java.net.InetAddress;
import java.util.List;

public interface MemberDiscovery {
    List<InetAddress> find() throws Exception;
}
