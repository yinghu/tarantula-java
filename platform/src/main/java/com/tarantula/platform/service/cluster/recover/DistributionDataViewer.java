package com.tarantula.platform.service.cluster.recover;

import com.tarantula.platform.service.cluster.ClusterDataView;

public interface DistributionDataViewer {

   void scan(String source, byte[] key, ClusterDataView view);
}
