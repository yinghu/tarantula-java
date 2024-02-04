package com.icodesoftware.service;

import java.util.List;

public interface Batchable {
    int size();
    List<byte[]> key();
    List<byte[]> data();

}
