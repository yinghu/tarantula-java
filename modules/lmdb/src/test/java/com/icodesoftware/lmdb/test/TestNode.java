package com.icodesoftware.lmdb.test;

import com.google.gson.JsonObject;
import com.icodesoftware.service.ClusterProvider;

import java.util.Map;

public class TestNode implements ClusterProvider.Node {
    @Override
    public long distributionId() {
        return 0;
    }

    @Override
    public void distributionId(long distributionId) {

    }

    @Override
    public String distributionKey() {
        return null;
    }

    @Override
    public void distributionKey(String distributionKey) {

    }

    @Override
    public int routingNumber() {
        return 0;
    }

    @Override
    public void routingNumber(int routingNumber) {

    }

    @Override
    public int scope() {
        return 0;
    }

    @Override
    public void index(String index) {

    }

    @Override
    public String index() {
        return null;
    }

    @Override
    public byte[] toBinary() {
        return new byte[0];
    }

    @Override
    public void fromBinary(byte[] payload) {

    }

    @Override
    public Map<String, Object> toMap() {
        return null;
    }

    @Override
    public void fromMap(Map<String, Object> properties) {

    }

    @Override
    public JsonObject toJson() {
        return null;
    }



    @Override
    public Key ownerKey() {
        return null;
    }

    @Override
    public void ownerKey(Key ownerKey) {

    }

    @Override
    public String owner() {
        return null;
    }

    @Override
    public void owner(String owner) {

    }

    @Override
    public boolean disabled() {
        return false;
    }

    @Override
    public void disabled(boolean disabled) {

    }

    @Override
    public String label() {
        return null;
    }

    @Override
    public void label(String label) {

    }

    @Override
    public long timestamp() {
        return 0;
    }

    @Override
    public void timestamp(long timestamp) {

    }

    @Override
    public long revision() {
        return 0;
    }

    @Override
    public void revision(long revision) {

    }

    @Override
    public boolean onEdge() {
        return false;
    }

    @Override
    public void onEdge(boolean onEdge) {

    }

    @Override
    public int getFactoryId() {
        return 0;
    }

    @Override
    public int getClassId() {
        return 0;
    }

    @Override
    public Key key() {
        return null;
    }

    @Override
    public String bucketName() {
        return null;
    }

    @Override
    public String nodeName() {
        return null;
    }

    @Override
    public long bucketId() {
        return 0;
    }

    @Override
    public long nodeId() {
        return 1999;
    }

    @Override
    public String memberId() {
        return null;
    }

    @Override
    public String address() {
        return null;
    }

    @Override
    public long startTime() {
        return 0;
    }

    @Override
    public long deploymentId() {
        return 0;
    }

    @Override
    public int partitionNumber() {
        return 0;
    }

    @Override
    public String clusterNameSuffix() {
        return null;
    }

    @Override
    public String deployDirectory() {
        return null;
    }

    @Override
    public String servicePushAddress() {
        return null;
    }

    @Override
    public boolean runAsMirror() {
        return false;
    }

    @Override
    public boolean backupEnabled() {
        return false;
    }

    @Override
    public boolean dailyBackupEnabled() {
        return false;
    }

    @Override
    public String dataStoreDirectory() {
        return null;
    }
}
