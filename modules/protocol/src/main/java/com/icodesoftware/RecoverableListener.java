package com.icodesoftware;

/**
 * Updated by yinghu lu on 8/1/2020.
 */
public interface RecoverableListener extends RecoverableRegistry{

    void onUpdated(int classId, String key, byte[] value);
    void addRecoverableFilter(int classId,Filter recoverableFilter);

    interface Filter {
        void on(Recoverable updated);
    }
}
