package com.tarantula.platform.service.cluster;

import com.tarantula.platform.event.TransactionReplicationEvent;

public interface DistributionReplicator {
    void replicate(TransactionReplicationEvent transactionReplicationEvent);
}
