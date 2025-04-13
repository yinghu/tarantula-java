package com.icodesoftware.lmdb.ffm;

import com.icodesoftware.Recoverable;

public interface NativeDataPairWriter {
    void onBuffer(Recoverable.DataBuffer buffer1,Recoverable.DataBuffer buffer2);
}
