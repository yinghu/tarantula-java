package com.tarantula;

/**
 * Created by yinghu lu on 7/15/2018.
 */
public interface RecoverableListener extends RecoverableRegistry{

    void onUpdated(Metadata metadata, byte[] key, byte[] value);

    void addRecoverableFilter(int classId,Filter recoverableFilter);
    void removeRecoverableFilter(int classId);
    interface Filter {
        void on(Recoverable updated);
    }
}
