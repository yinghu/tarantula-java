package com.icodesoftware.lmdb;

import com.icodesoftware.Recoverable;

public class LocalHeader implements Recoverable.DataHeader {

    private boolean local;
    private long revision;

    private final int factoryId;

    private final int classId;
    public LocalHeader(boolean local,long revision,int factoryId, int classId){
        this.local = local;
        this.revision = revision;
        this.factoryId = factoryId;
        this.classId = classId;
    }

    @Override
    public boolean local() {
        return local;
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

    public void update(boolean local,long revisionDelta){
        this.local = local;
        revision = revision+revisionDelta;
    }
}
