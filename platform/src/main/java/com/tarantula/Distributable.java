package com.tarantula;

/**
 * Updated by yinghu on 4/6/2018.
 */
public interface Distributable {

    int LOCAL_SCOPE = 0; //NO REPLICATION
    int DATA_SCOPE = 1; //REPLICATION WITHIN DATA CLUSTER
    int INTEGRATION_SCOPE = 2; //REPLICATION WITHIN INTEGRATION CLUSTER

    String bucket();
    void bucket(String bucket);

    String distributionKey();
    void distributionKey(String distributionKey);

    int routingNumber();
    void routingNumber(int routingNumber);

    int scope();

    void distributable(boolean distributable);
    void index(String index);
    boolean distributable();
    String index();
}
