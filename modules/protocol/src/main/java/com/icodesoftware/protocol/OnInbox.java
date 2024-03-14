package com.icodesoftware.protocol;

import com.icodesoftware.OnAccess;

import java.util.List;

public interface OnInbox {
    String name();
    String category();
    List<OnAccess> content();
}
