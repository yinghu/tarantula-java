package com.tarantula;

/**
 * Updated by yinghu on 4/24/2018.
 */
public interface OnBalance extends OnApplication {

    /**
     * The balance of the event
     * */

    String event();
    void event(String event);

    void redeemed(boolean redeemed);
    boolean redeemed();
}
