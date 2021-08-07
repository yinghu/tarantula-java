package com.icodesoftware;

public interface OnApplication extends Response {

    int accessMode();
    void accessMode(int mode);

    String systemId();
    void systemId(String systemId);

    int stub();
    void stub(int stub);

    String name();
    void name(String name);


    String tournamentId();
    void tournamentId(String tournamentId);

    double balance();
    void balance(double balance);

    String typeId();
    void typeId(String typeId);

    String ticket();
    void ticket(String ticket);

}
