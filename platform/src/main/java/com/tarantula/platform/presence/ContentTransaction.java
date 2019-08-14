package com.tarantula.platform.presence;

import com.tarantula.Recoverable;
import com.tarantula.platform.AssociateKey;
import com.tarantula.platform.NoReplicationObject;
import com.tarantula.platform.RecoverableObject;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Created by yinghu lu on 4/26/2018.
 */
public class ContentTransaction extends RecoverableObject {

    public static String AVATAR = "Avatar";

    public int contentSize;
    public int batchSize;
    public int chunkSize;
    public String contentType;

    public int _batched;
    public int _payloadSize;

    public ContentTransaction(){

    }
    public ContentTransaction(String owner,String type){
        this.vertex = type;
        this.distributionKey(owner);

    }
    @Override
    public int getFactoryId() {
        return UserPortableRegistry.OID;
    }

    @Override

    public int getClassId() {
        return UserPortableRegistry.CONTENT_TRANSACTION_CID;
    }

    @Override
    public Map<String,Object> toMap(){
        this.properties.put("1",this.vertex);
        this.properties.put("2",contentType);
        this.properties.put("3",chunkSize);
        this.properties.put("4",batchSize);
        this.properties.put("5",contentSize);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.vertex = (String)properties.get("1");
        this.contentType = (String)properties.get("2");
        this.chunkSize = ((Number)properties.get("3")).intValue();
        this.batchSize = ((Number)properties.get("4")).intValue();
        this.contentSize = ((Number)properties.get("5")).intValue();
    }

    public byte[] toByteArray(){
        byte[] ct = contentType.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(16+ct.length);
        buffer.putInt(contentSize);
        buffer.putInt(batchSize);
        buffer.putInt(chunkSize);
        buffer.putInt(ct.length);
        buffer.put(ct);
        return buffer.array();
    }
    public void fromByteArray(byte[] data){
        ByteBuffer buffer = ByteBuffer.wrap(data);
        this.contentSize = buffer.getInt();
        this.batchSize = buffer.getInt();
        this.chunkSize = buffer.getInt();
        int len = buffer.getInt();
        StringBuffer sb = new StringBuffer();
        for(int i=0;i<len;i++){
            sb.append((char) buffer.get());
        }
        this.contentType = sb.toString();
    }

    public synchronized boolean onTransaction(int payloadSize){
        _batched--;
        _payloadSize = _payloadSize+payloadSize;
        boolean done = _batched==0;
        if(done){
            contentSize = _payloadSize;
        }
        return done;
    }

    @Override
    public Recoverable.Key key(){
        return new AssociateKey(this.bucket,this.oid,this.vertex);
    }
    @Override
    public String toString(){
        return "Content->"+contentType+"/"+contentSize+"/"+batchSize+"/"+chunkSize;
    }
}
