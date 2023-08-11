package com.tarantula.platform.service.cluster.recover;

import com.icodesoftware.service.ClusterProvider;

public interface DistributionDataViewer {

    byte[] load(String source, byte[] key, ClusterProvider.Node node);
}
