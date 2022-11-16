package com.icodesoftware.service;

import com.icodesoftware.Recoverable;

public interface OnReplication {
    String source();
    int partition();
    byte[] key();
    byte[] value();
    String keyAsString();
    Recoverable recoverable();
}
