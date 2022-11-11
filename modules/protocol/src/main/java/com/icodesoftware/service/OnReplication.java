package com.icodesoftware.service;

public interface OnReplication {
    String source();
    int partition();
    byte[] key();
    byte[] value();
}
