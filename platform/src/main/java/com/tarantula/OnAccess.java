package com.tarantula;

/**
 * Updated by yinghu on 7/3/2018.
 */
public interface OnAccess extends OnApplication, OnProperty {

    String accessKey();
    void accessKey(String accessKey);
    String accessId();
    void accessId(String accessId);//data id

}
