package com.tarantula;

/**
 * Updated by yinghu lu on 8/1/2020.
 */
public interface RecoverableListener extends RecoverableRegistry{

    void onUpdated(Metadata metadata, byte[] key, byte[] value);
    void addRecoverableFilter(int classId,Filter recoverableFilter);
    void removeRecoverableFilter(int classId);
    interface Filter {
        void on(Recoverable updated);
    }
}
