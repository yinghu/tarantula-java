package com.icodesoftware;

public interface OnApplication extends Response {


    String systemId();
    void systemId(String systemId);

    long stub();
    void stub(long stub);

    String name();
    void name(String name);

    long tournamentId();
    void tournamentId(long tournamentId);


    String typeId();
    void typeId(String typeId);

    String ticket();
    void ticket(String ticket);

}
