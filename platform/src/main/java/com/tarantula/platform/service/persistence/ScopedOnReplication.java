package com.tarantula.platform.service.persistence;

import com.icodesoftware.service.OnReplication;

public interface ScopedOnReplication {

    OnReplication read();
    void drop();
}
