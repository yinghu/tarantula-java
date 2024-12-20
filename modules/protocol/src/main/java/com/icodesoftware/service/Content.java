package com.icodesoftware.service;

public interface Content {
    byte[] data();
    String type();
    boolean existed();

    String fileName();
    int revisionNumber();
}
