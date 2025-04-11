package com.icodesoftware.lmdb.ffm;

import com.icodesoftware.DataStore;
import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;

public class NativeDataStore {



    public <T extends Recoverable> boolean create(T t) {
        return false;
    }


    public <T extends Recoverable> void list(RecoverableFactory<T> query, DataStore.Stream<T> stream) {

    }
}
