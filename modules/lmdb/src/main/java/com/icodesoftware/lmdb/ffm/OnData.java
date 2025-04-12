package com.icodesoftware.lmdb.ffm;

import com.icodesoftware.Recoverable;

public interface OnData{
    void fill(Recoverable.DataBuffer buffer);
}
