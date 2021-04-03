package com.icodesoftware;

public interface RecoverableListener extends RecoverableRegistry{

    void onUpdated(int classId,String owner,String key, byte[] value);
    void addRecoverableFilter(int classId,Filter recoverableFilter);

    interface Filter {
        void on(Recoverable updated);
    }
}
