package com.tarantula;


import com.icodesoftware.DataStore;

/**
 * Developer: YINGHU LU
 * Date: Updated 3/6/2019
 * Time: 8:28 PM
 */
public interface OnInstance extends OnBalance, DataStore.Updatable{

    String LABEL = "IOI";
    boolean joined();

    void joined(boolean joined);

    void reset(double reset);

    boolean transact(double delta);

    int idle(boolean reset);

    interface Listener {
        void onUpdated(OnInstance onInstance);
    }
}
