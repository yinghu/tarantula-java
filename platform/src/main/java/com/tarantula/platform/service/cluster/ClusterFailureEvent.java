package com.tarantula.platform.service.cluster;

import com.tarantula.platform.service.ServiceEventLog;

public class ClusterFailureEvent extends ServiceEventLog {

    public ClusterFailureEvent(String source,Exception exception){
        super("cluster",Level.CRITICAL,source,exception);
    }
}
