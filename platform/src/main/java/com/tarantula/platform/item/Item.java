package com.tarantula.platform.item;

import com.icodesoftware.Consumable;

public class Item implements Consumable {

    @Override
    public String id() {
        return null;
    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public String description() {
        return null;
    }

    @Override
    public <T extends Object> void property(String s, T t) {

    }

    @Override
    public <T extends Object> T property(String s) {
        return null;
    }
}
