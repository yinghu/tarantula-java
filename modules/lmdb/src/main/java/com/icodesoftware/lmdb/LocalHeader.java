package com.icodesoftware.lmdb;

import com.icodesoftware.Recoverable;

public class LocalHeader implements Recoverable.DataHeader {

    private long revision;

    private final int factoryId;
    private final int classId;
    public LocalHeader(long revision,int factoryId, int classId){
        this.revision = revision;
        this.factoryId = factoryId;
        this.classId = classId;
    }
    @Override
    public long revision() {
        return revision;
    }

    @Override
    public int factoryId() {
        return factoryId;
    }

    @Override
    public int classId() {
        return classId;
    }

    public void update(long revisionDelta){
        revision = revision+revisionDelta;
    }
}
