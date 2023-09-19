package com.tarantula.platform.service.cluster.accessindex;

import com.icodesoftware.service.ClusterProvider;

public interface DistributionAccessIndexViewer {
    byte[] load(String source, byte[] key, ClusterProvider.Node node);
}
