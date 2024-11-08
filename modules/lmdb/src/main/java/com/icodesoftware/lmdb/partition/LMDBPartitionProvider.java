package com.icodesoftware.lmdb.partition;

import com.icodesoftware.Recoverable;
import com.icodesoftware.lmdb.BufferProxy;
import com.icodesoftware.lmdb.EnvSetting;
import com.icodesoftware.service.Serviceable;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

public class LMDBPartitionProvider implements Serviceable {

    private int maxPartitionNumber = 10;
    private int storeMbSize = 1000; //1G
    private String basePath ="target/lmdb/partition";
    private String keyIndexStoreName = "partition_key_index";
    private final ConcurrentHashMap<Integer, LMDBPartitionDaemon> partitionMap = new ConcurrentHashMap<>();

    private LMDBPartitionDaemon keyIndex;
    private LMDBPartitionDaemon currentPartition;
    @Override
    public void start() throws Exception {
        for(int i=1;i<maxPartitionNumber;i++){
            Files.createDirectories(Paths.get(basePath+"/"+i));

            Files.createDirectories(Paths.get(basePath+"/"+i+"/back"));
        }
        Files.createDirectories(Paths.get(basePath+"/index"));
        Files.createDirectories(Paths.get(basePath+"/index/back"));
        keyIndex = new LMDBPartitionDaemon(envSetting(storeMbSize,basePath+"/index",1));
        keyIndex.start();
        LMDBPartitionDaemon partition1 = new LMDBPartitionDaemon(envSetting(storeMbSize,basePath+"/1",1));
        partition1.start();
        partitionMap.put(partition1.partition,partition1);
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

    public LMDBPartitionDaemon partition(ByteBuffer key){
        ByteBuffer partition = keyIndex.get(keyIndexStoreName,key);
        if(partition==null) return currentPartition;
        return partitionMap.get(partition.getInt());
    }
    public void onMapFull(LMDBPartitionDaemon lmdbPartition, ByteBuffer key){
        partitionMap.computeIfAbsent(lmdbPartition.partition+1,k->{
            LMDBPartitionDaemon next = new LMDBPartitionDaemon(envSetting(storeMbSize,basePath+"/"+k,k));
            try{
                next.start();
            }catch (Exception ex){
                throw new RuntimeException(ex);
            }
            currentPartition = next;
            return next;
        });
    }
    public void onPut(LMDBPartitionDaemon lmdbPartition, ByteBuffer key){
        Recoverable.DataBuffer partition = BufferProxy.buffer(4,true);
        keyIndex.put(keyIndexStoreName,key,partition.writeInt(lmdbPartition.partition).flip());
    }
    public void onDelete(LMDBPartitionDaemon lmdbPartition, ByteBuffer key){
        keyIndex.delete(keyIndexStoreName,key);
    }


    private EnvSetting envSetting(int mbSize,String storePath,int partition){
        return new EnvSetting(EnvSetting.data,storePath,mbSize,partition,true);
    }
}
