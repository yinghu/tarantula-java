package com.tarantula.platform.service.cluster;

import com.hazelcast.core.Member;

public interface ClusterDataView {
    boolean onData(Member member,byte[] key,byte[] value);
}
