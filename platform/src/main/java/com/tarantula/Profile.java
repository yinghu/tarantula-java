package com.tarantula;

/**
 * updated by yinghu on 4/17/2018
 */
public interface Profile extends Recoverable {


    void nickname(String nickname);
    String nickname();
    void avatar(String avatar);
    String avatar();
    void emailAddress(String emailAddress);
    String emailAddress();
    String video();
    void video(String video);
}
