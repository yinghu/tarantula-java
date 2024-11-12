package com.icodesoftware.lmdb.partition;

import com.icodesoftware.Recoverable;
import com.icodesoftware.lmdb.BufferProxy;
import com.icodesoftware.lmdb.EnvSetting;
import com.icodesoftware.lmdb.LocalDistributionIdGenerator;
import com.icodesoftware.service.Serviceable;
import com.icodesoftware.util.TimeUtil;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

public class LMDBPartitionProvider implements Serviceable {

    private int maxPartitionNumber = 10;
    private int storeMbSize = 1000; //1G
    private String basePath ="target/lmdb/partition";
    private String keyIndexStoreName = "partition_key_index";
    private final ConcurrentHashMap<Integer, LMDBPartition> partitionMap = new ConcurrentHashMap<>();

    private LMDBPartitionEnv keyIndex;
    private LMDBPartition currentPartition;
    private LocalDistributionIdGenerator localDistributionIdGenerator;

    private final boolean isProxy;

    private LMDBPartitionProvider(){
        this.isProxy = true;
    }
    private LMDBPartitionProvider(boolean isProxy){
        this.isProxy = isProxy;
    }

    @Override
    public void start() throws Exception {
        for(int i=1;i<maxPartitionNumber;i++){
            Files.createDirectories(Paths.get(basePath+"/"+i));
            Files.createDirectories(Paths.get(basePath+"/"+i+"/back"));
        }
        localDistributionIdGenerator = new LocalDistributionIdGenerator(1, TimeUtil.epochMillisecondsFromMidnight(2020,1,1));
        Files.createDirectories(Paths.get(basePath+"/index"));
        Files.createDirectories(Paths.get(basePath+"/index/back"));
        keyIndex = new LMDBPartitionEnv(envSetting(storeMbSize,basePath+"/index",1));
        keyIndex.start();
        LMDBPartition partition1 = isProxy? new LMDBPartitionProxy(1) : new LMDBPartitionEnv(envSetting(storeMbSize,basePath,1));
        partition1.start();
        partitionMap.put(partition1.partition(),partition1);
        currentPartition = partition1;
    }

    @Override
    public void shutdown() throws Exception {
        keyIndex.shutdown();
        partitionMap.forEach((k,p)->{
            try {
                p.shutdown();
            }catch (Exception ex){
                //ignore
            }
        });
    }

    public LMDBPartition partition(ByteBuffer key){
        ByteBuffer partition = keyIndex.get(keyIndexStoreName,key);
        if(partition==null) return currentPartition;
        return partitionMap.get(partition.getInt());
    }
    public void onMapFull(LMDBPartition lmdbPartition, ByteBuffer key){
        partitionMap.computeIfAbsent(lmdbPartition.partition()+1,k->{
            LMDBPartitionEnv next = new LMDBPartitionEnv(envSetting(storeMbSize,basePath+"/"+k,k));
            try{
                next.start();
            }catch (Exception ex){
                throw new RuntimeException(ex);
            }
            currentPartition = next;
            return next;
        });
    }
    public void onPut(LMDBPartition lmdbPartition, ByteBuffer key){
        Recoverable.DataBuffer partition = BufferProxy.buffer(4,true);
        keyIndex.put(keyIndexStoreName,key,partition.writeInt(lmdbPartition.partition()).flip());
    }
    public void onDelete(LMDBPartition lmdbPartition, ByteBuffer key){
        keyIndex.delete(keyIndexStoreName,key);
    }

    public void assign(Recoverable.DataBuffer key){
        key.writeLong(localDistributionIdGenerator.id());
        key.flip();
    }

    private EnvSetting envSetting(int mbSize,String storePath,int partition){
        return new EnvSetting(EnvSetting.data,storePath,mbSize,partition,true);
    }

    public static LMDBPartitionProvider create(boolean isProxy){
        return new LMDBPartitionProvider(isProxy);
    }

    public static LMDBPartitionProvider create(){
        return new LMDBPartitionProvider();
    }
}
