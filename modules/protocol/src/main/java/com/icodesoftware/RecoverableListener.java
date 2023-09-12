package com.icodesoftware;

public interface RecoverableListener extends RecoverableRegistry{

    void onUpdated(Recoverable recoverable);
    void addRecoverableFilter(int classId,Filter recoverableFilter);

    interface Filter {
        void on(Recoverable updated);
    }
}
