package com.icodesoftware.service;

public interface ReloadListener {
    void reload(int partition,boolean localMember);
}
