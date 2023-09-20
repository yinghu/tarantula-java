package com.icodesoftware.service;

import com.icodesoftware.Recoverable;

public interface OnReplication {
    int scope();
    int factoryId();
    int classId();
    String nodeName();
    String source();
    //int partition();
    byte[] key();
    byte[] value();
    String keyAsString();
    Recoverable recoverable();
}
