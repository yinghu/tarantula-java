package com.icodesoftware;

public interface RecoverableListener extends RecoverableRegistry{

    void onUpdated(Recoverable recoverable);
    void addRecoverableFilter(int classId,Filter recoverableFilter);

    interface Filter<T> {
        void on(T updated);
    }
}
