package com.tarantula;

/**
 * Updated by yinghu on 4/24/2018.
 */
public interface OnPlay extends OnBalance {

    /**
     * The category of games played such as craps, slots
     * */
    String category();
    void category(String category);
}
