package com.tarantula.platform.service.cluster.accessindex;


import com.tarantula.platform.service.cluster.ClusterDataView;


public interface DistributionAccessIndexViewer {
    void scan(byte[] key, ClusterDataView view);

}
