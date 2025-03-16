package com.icodesoftware.util;

import com.icodesoftware.Countable;

public class TRCountable extends RecoverableObject implements Countable {

    protected int count;

    @Override
    public int count(int delta) {
        count = count+(delta);
        return count;
    }
}
