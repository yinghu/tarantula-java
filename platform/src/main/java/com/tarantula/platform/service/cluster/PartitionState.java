package com.tarantula.platform.service.cluster;

import com.tarantula.platform.service.OnPartition;

public class PartitionState implements OnPartition {
    public final int partition;
    public boolean opening;
    public int version;
    public PartitionState(int partition,boolean opening){
        this.partition = partition;
        this.opening = opening;
    }

    @Override
    public int partition() {
        return this.partition;
    }

    @Override
    public boolean opening() {
        return this.opening;
    }
}
