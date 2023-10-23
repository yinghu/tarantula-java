package com.icodesoftware;

public interface Distributable {

    int LOCAL_SCOPE = 0; //NO REPLICATION

    int DATA_SCOPE = 1; //USER DATA REPLICATION SCOPE

    int INTEGRATION_SCOPE = 2; //ACCESS REPLICATION SCOPE
    int INDEX_SCOPE = 3; //KEY INDEX REPLICATION SCOPE
    int LOG_SCOPE = 4; //TRANSACTION LOG SCOPE

    long distributionId();
    void distributionId(long distributionId);


    String distributionKey();
    void distributionKey(String distributionKey);

    int routingNumber();
    void routingNumber(int routingNumber);


    int scope();

    void index(String index);

    String index();

    // marked as cluster distributing
    boolean distributable();

    byte[] toBinary();
    void fromBinary(byte[] payload);
}
