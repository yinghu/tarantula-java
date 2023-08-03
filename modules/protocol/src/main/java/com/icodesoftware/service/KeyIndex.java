package com.icodesoftware.service;

import com.icodesoftware.Recoverable;

public interface KeyIndex extends Recoverable {
    String masterNode();
    String[] slaveNodes();
}
