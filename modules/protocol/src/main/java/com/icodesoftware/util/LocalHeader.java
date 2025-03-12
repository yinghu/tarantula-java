package com.icodesoftware.util;

import com.icodesoftware.Recoverable;

import java.util.Arrays;

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

    @Override
    public int hashCode() {
        return Arrays.hashCode(new int[]{factoryId,classId});
    }

    @Override
    public boolean equals(Object obj) {
        LocalHeader localHeader = (LocalHeader)obj;
        return factoryId==localHeader.factoryId() && classId == localHeader.classId();
    }

    public static Recoverable.DataHeader create(int factoryId, int classId){
        return new LocalHeader(0,factoryId,classId);
    }

    public static Recoverable.DataHeader create(int factoryId,int classId,long revision){
        return new LocalHeader(revision,factoryId,classId);
    }
}
