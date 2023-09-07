package com.icodesoftware.service;

import com.icodesoftware.Recoverable;

import java.nio.ByteBuffer;


public interface MapStoreListener extends ServiceProvider {

    //dispatch backup operation
    <T extends Recoverable> void onBackingUp(Metadata metadata,String key,T t);

    //dispatch cluster operation
    void onDistributing(Metadata metadata,String stringKey, byte[] key, byte[] value);

    void onDistributing(Metadata metadata, ByteBuffer key,ByteBuffer value);

    //recover cluster operation
    byte[] onRecovering(Metadata metadata,String stringKey,byte[] key);

    void onDeleting(Metadata metadata,byte[] key);

    void assignKey(Recoverable.DataBuffer dataBuffer);

    long distributionId();
}
