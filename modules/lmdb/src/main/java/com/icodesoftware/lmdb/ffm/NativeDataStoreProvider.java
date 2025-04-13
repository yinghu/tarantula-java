package com.icodesoftware.lmdb.ffm;

import com.icodesoftware.DataStore;
import com.icodesoftware.Recoverable;
import com.icodesoftware.Transaction;
import com.icodesoftware.lmdb.LocalDistributionIdGenerator;
import com.icodesoftware.service.DataStoreProvider;
import com.icodesoftware.service.MapStoreListener;
import com.icodesoftware.service.Metadata;
import com.icodesoftware.util.TimeUtil;

import java.io.File;
import java.util.List;
import java.util.Map;

public class NativeDataStoreProvider implements DataStoreProvider{

    private int KEY_SIZE = 200;
    private int VALUE_SIZE = 1780;

    private DataStoreProvider.DistributionIdGenerator distributionIdGenerator = new LocalDistributionIdGenerator(1,TimeUtil.epochMillisecondsFromMidnight(2020,1,1));;

    @Override
    public void configure(Map<String, Object> properties) {

    }

    public void registerDistributionIdGenerator(DataStoreProvider.DistributionIdGenerator distributionIdGenerator){
        if(distributionIdGenerator!=null) this.distributionIdGenerator = distributionIdGenerator;
    }

    @Override
    public void registerMapStoreListener(int scope, MapStoreListener mapStoreListener) {

    }

    @Override
    public File backup(int scope) {
        return null;
    }

    @Override
    public DataStore createAccessIndexDataStore(String name) {
        return null;
    }

    @Override
    public DataStore createKeyIndexDataStore(String name) {
        return null;
    }

    @Override
    public DataStore createDataStore(String name) {
        return null;
    }

    @Override
    public DataStore createLocalDataStore(String name) {
        return null;
    }

    @Override
    public DataStore createLogDataStore(String name) {
        return null;
    }

    @Override
    public List<String> list() {
        return List.of();
    }

    @Override
    public List<String> list(int scope) {
        return List.of();
    }

    @Override
    public DataStore lookup(String name) {
        return null;
    }

    @Override
    public Transaction transaction(int scope) {
        return null;
    }

    @Override
    public Recoverable.DataBufferPair dataBufferPair() {
        return null;
    }

    public void assign(Recoverable.DataBuffer dataBuffer){
        this.distributionIdGenerator.assign(dataBuffer);
    }

    @Override
    public long storeSize() {
        return 0;
    }

    @Override
    public int maxReaderNumber() {
        return 0;
    }

    @Override
    public int maxDatabaseNumber() {
        return 0;
    }

    @Override
    public boolean diskSyncOnCommit() {
        return false;
    }


    @Override
    public void onUpdating(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value, long transactionId) {

    }

    @Override
    public boolean onRecovering(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value) {
        return false;
    }

    @Override
    public boolean onRecovering(Metadata metadata, Recoverable.DataBuffer key, DataStore.BufferStream bufferStream) {
        return false;
    }

    @Override
    public boolean onDeleting(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value, long transactionId) {
        return false;
    }

    @Override
    public void onCommit(int scope, long transactionId) {

    }

    @Override
    public void onAbort(int scope, long transactionId) {

    }

    @Override
    public void onUpdated(String category, double delta) {

    }

    @Override
    public String name() {
        return "";
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }
}
