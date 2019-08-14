package com.tarantula.platform.service;

import java.net.InetAddress;
import java.util.List;

/**
 * Created by yinghu lu on 8/10/2018.
 */
public interface MemberDiscovery {
    List<InetAddress> find() throws Exception;
}
