package com.icodesoftware.lmdb;

import com.icodesoftware.service.Metadata;
import org.lmdbjava.Dbi;

import java.nio.ByteBuffer;

public class LocalEdgeDataStore {

    public final Metadata metadata;
    public final Dbi<ByteBuffer> dbi;

    public LocalEdgeDataStore(final Metadata metadata,final Dbi<ByteBuffer> dbi){
        this.metadata = metadata;
        this.dbi = dbi;
    }
}
