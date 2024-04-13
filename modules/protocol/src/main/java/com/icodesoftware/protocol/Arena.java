package com.icodesoftware.protocol;

import com.icodesoftware.JsonSerializable;

public interface Arena extends JsonSerializable {

    int xp();
    int level();
    String name();
}
