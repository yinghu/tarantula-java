package com.tarantula;

/**
 * Updated by yinghu on 5/15/2020
 */
public interface OnAccess extends OnApplication {

    String accessKey();
    void accessKey(String accessKey);
    String accessId();
    void accessId(String accessId);//data id

    byte[] payload();
    void payload(byte[] payload);

    Object property(String name);
    void property(String name,Object value);
}
