package com.icodesoftware.util;

import com.icodesoftware.service.ClusterProvider;

public class AbstractClusterNode extends RecoverableObject implements ClusterProvider.Node {

    protected String bucketName;
    protected String nodeName;

    protected long bucketId;
    protected long nodeId;
    protected String memberId;
    protected String address;
    protected long startTime;

    protected long deploymentId;
    protected String clusterNameSuffix;
    protected int partitionNumber;
    protected int bucketNumber;
    protected String deployDirectory;
    protected String servicePushAddress;

    protected boolean dailyBackupEnabled;
    protected String dataStoreDirectory;

    protected int clusterSize;
    protected int pingCount;
    protected String etcdHost;
    @Override
    public String bucketName() {
        return bucketName;
    }

    @Override
    public String nodeName() {
        return nodeName;
    }

    @Override
    public long bucketId() {
        return bucketId;
    }

    @Override
    public long nodeId() {
        return nodeId;
    }

    @Override
    public String memberId() {
        return memberId;
    }

    @Override
    public String address() {
        return address;
    }

    @Override
    public long startTime() {
        return startTime;
    }

    @Override
    public long deploymentId() {
        return deploymentId;
    }

    @Override
    public int partitionNumber() {
        return partitionNumber;
    }

    @Override
    public int bucketNumber() {
        return bucketNumber;
    }

    @Override
    public String clusterNameSuffix() {
        return clusterNameSuffix;
    }

    @Override
    public String deployDirectory() {
        return deployDirectory;
    }

    @Override
    public String servicePushAddress() {
        return servicePushAddress;
    }

    @Override
    public boolean dailyBackupEnabled() {
        return dailyBackupEnabled;
    }

    @Override
    public String dataStoreDirectory() {
        return dataStoreDirectory;
    }

    @Override
    public ClusterProvider.HomingAgent homingAgent() {
        return null;
    }

    public int clusterSize(){
        return clusterSize;
    }
    public int pingCount(){
        return pingCount;
    }

    public String etcdHost(){
        return etcdHost;
    }
    public void bucketName(String bucketName){
        this.bucketName = bucketName;
    }
    public void nodeName(String nodeName){
        this.nodeName = nodeName;
    }
    public void deploymentId(long deploymentId){
        this.deploymentId = deploymentId;
    }

    public void clusterNameSuffix(String clusterNameSuffix) {
        this.clusterNameSuffix = clusterNameSuffix;
    }

    public void deployDirectory(String deployDirectory) {
        this.deployDirectory = deployDirectory;
    }


    public void servicePushAddress(String servicePushAddress) {
        this.servicePushAddress = servicePushAddress;
    }

    public void dailyBackupEnabled(boolean dailyBackupEnabled) {
        this.dailyBackupEnabled = dailyBackupEnabled;
    }

    public void dataStoreDirectory(String dataStoreDirectory) {
        this.dataStoreDirectory = dataStoreDirectory;
    }

    public void bucketId(long bucketId){
        this.bucketId = bucketId;
    }

    public void nodeId(long nodeId){
        this.nodeId = nodeId;
    }

    public void memberId(String memberId){
        this.memberId = memberId;
    }

    public void startTime(long startTime){
        this.startTime = startTime;
    }

    public void address(String address){
        this.address = address;
    }

    public void clusterSize(int clusterSize){
        this.clusterSize = clusterSize;
    }
    public  void pingCount(int pingCount){
        this.pingCount = pingCount;
    }
    public void etcdHost(String etcdHost){
        this.etcdHost = etcdHost;
    }



}
