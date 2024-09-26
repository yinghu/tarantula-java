package com.icodesoftware;

public interface Achievement extends JsonSerializable{
    String name();
    int tier();
    int target();

    double objective();

    void onProgress(double delta);
}
