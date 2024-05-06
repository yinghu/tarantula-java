package com.icodesoftware;

public interface Achievement{
    String name();
    int tier();
    int target();

    double objective();

    Achievement onProgress(double delta);
}
