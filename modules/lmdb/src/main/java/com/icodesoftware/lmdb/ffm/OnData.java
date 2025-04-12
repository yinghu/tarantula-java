package com.icodesoftware.lmdb.ffm;

import com.icodesoftware.Recoverable;

public interface OnData{
    void onBuffer(Recoverable.DataBuffer buffer);
}
