package com.tarantula;

/**
 * updated by yinghu on 8/26/19
 */
public interface Profile extends Recoverable, OnProperty {

    void nickname(String nickname);
    String nickname();
    void avatar(String avatar);
    String avatar();
    void emailAddress(String emailAddress);
    String emailAddress();
}
