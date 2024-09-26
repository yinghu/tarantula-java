package com.icodesoftware.service;

public interface ReloadListener {
    default void onPartition(int partition,boolean localMember){}
    default void onBucket(int bucket,boolean opening){}
    default void onReload(){}
}
