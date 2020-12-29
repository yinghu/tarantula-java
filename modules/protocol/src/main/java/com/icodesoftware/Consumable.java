package com.icodesoftware;

public interface Consumable {
    String id();
    String name();
    String description();
    <T extends Object> void property(String name,T value);
    <T extends Object> T property(String name);
}
