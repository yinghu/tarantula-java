package com.icodesoftware.service;

public interface KeyIndexService extends ServiceProvider{

    String NAME = "KeyIndexService";

    KeyIndex setIfAbsent(String key);
}
