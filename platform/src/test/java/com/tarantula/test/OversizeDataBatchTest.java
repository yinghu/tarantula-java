package com.tarantula.test;

import com.google.gson.JsonObject;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.presence.saves.BatchedMappingObject;
import com.tarantula.platform.presence.saves.OversizeDataBatch;
import jnr.ffi.annotations.In;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.HashMap;


public class OversizeDataBatchTest extends DataStoreHook{


    @Test(groups = { "OversizeDataBatch" })
    public void oversizeBatchTest(){
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("sample-admin-role-commodity-settings.json");
        JsonObject json  = JsonUtil.parse(in);
        Assert.assertNotNull(json);
        byte[] data = json.toString().getBytes();
        Assert.assertTrue(data.length>1600);
        HashMap<Integer,byte[]> batch = OversizeDataBatch.batch(data,1600);
        StringBuffer buffer = new StringBuffer();
        HashMap<Integer, BatchedMappingObject> indexed = new HashMap<>();

        for(int i=0;i<batch.size();i++){
            byte[] chunk = batch.get(i);
            if(chunk!=null){
                buffer.append(new String(chunk));
                BatchedMappingObject mo = new BatchedMappingObject();
                mo.batch = i;
                mo.value(chunk);
                indexed.put(i,mo);
            }
        }
        JsonObject reverse = JsonUtil.parse(buffer.toString());
        Assert.assertEquals(json.toString(),reverse.toString());
        byte[] stream = OversizeDataBatch.batch(indexed);
        JsonObject reverseFromIndex = JsonUtil.parse(stream);
        Assert.assertEquals(reverse.toString(),reverseFromIndex.toString());
    }
    @Test(groups = { "OversizeDataBatch" })
    public void shortBatchTest() throws Exception{
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("replication-service-settings.json");
        JsonObject json  = JsonUtil.parse(in);
        Assert.assertNotNull(json);
        byte[] data = json.toString().getBytes();
        Assert.assertTrue(data.length<=1600);
        HashMap<Integer,byte[]> batch = OversizeDataBatch.batch(data,1600);
        HashMap<Integer, BatchedMappingObject> indexed = new HashMap<>();

        StringBuffer buffer = new StringBuffer();

        for(int i=0;i<batch.size();i++){
            byte[] chunk = batch.get(i);
            if(chunk!=null){
                buffer.append(new String(chunk));
                BatchedMappingObject mo = new BatchedMappingObject();
                mo.batch = i;
                mo.value(chunk);
                indexed.put(i,mo);
            }
        }
        JsonObject reverse = JsonUtil.parse(buffer.toString());
        Assert.assertEquals(json.toString(),reverse.toString());
        byte[] stream = OversizeDataBatch.batch(indexed);
        JsonObject reverseFromIndex = JsonUtil.parse(stream);
        Assert.assertEquals(reverse.toString(),reverseFromIndex.toString());
    }

}
