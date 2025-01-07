package com.icodesoftware.etcd;

public class EtcdConfiguration {

    //kickoff of ping lost count
    public int pingCount = 3;

    //join loop count
    public int joinTimer = 10;

    //ping per second
    public int pingTimer = 1;

    public int clusterSize = 10;
    public int partitionNumber = 17;

}
