package com.icodesoftware.lmdb.ffm;

import com.icodesoftware.Recoverable;

public interface NativeDataWriter {
    void onBuffer(Recoverable.DataBuffer buffer);
}
