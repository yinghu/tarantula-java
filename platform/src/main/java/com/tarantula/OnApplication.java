package com.tarantula;


/**
 * Developer: YINGHU LU
 * Updated 8/14/19
 *
 * On Application represents an application state of the player
 */

public interface OnApplication extends Recoverable{

    int accessMode();
    void accessMode(int mode);

    String systemId();
    void systemId(String systemId);

    int stub();
    void stub(int stub);

    String name();
    void name(String name);

    String applicationId();
    void applicationId(String applicationId);

    String instanceId();
    void instanceId(String instanceId);

    double balance();
    void balance(double balance);

    double entryCost();
    void entryCost(double entryCost);

    String typeId();
    void typeId(String typeId);

    String subtypeId();
    void subtypeId(String subtypeId);

    boolean tournamentEnabled();
    void tournamentEnabled(boolean tournamentEnabled);

}
