package com.tarantula.platform.store;

import com.google.gson.JsonObject;
import com.icodesoftware.Recoverable;
import com.icodesoftware.util.NaturalKey;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.SnowflakeKey;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.item.ItemPortableRegistry;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StoreTransactionLog extends RecoverableObject {

    public static final String LABEL = "iap_transaction";
    public String transactionId;

    public long playerId;
    public long itemId;
    public boolean granted;
    public String storeName;

    public StoreTransactionLog(){
        this.onEdge = true;
        this.label = LABEL;
    }
    public StoreTransactionLog(String transactionId){
        this();
        this.transactionId = transactionId;
    }
    public StoreTransactionLog(long playerId,String storeName,String transactionId,long itemId,boolean granted){
        this();
        this.playerId = playerId;
        this.storeName = storeName;
        this.transactionId = transactionId;
        this.itemId = itemId;
        this.granted = granted;
        this.timestamp = TimeUtil.toUTCMilliseconds(LocalDateTime.now());
        this.ownerKey = SnowflakeKey.from(playerId);
    }

    public int getFactoryId() {
        return ItemPortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return ItemPortableRegistry.STORE_TRANSACTION_LOG_CID;
    }


    @Override
    public boolean read(DataBuffer buffer){
        this.playerId = buffer.readLong();
        this.storeName = buffer.readUTF8();
        this.itemId = buffer.readLong();
        this.granted = buffer.readBoolean();
        this.timestamp = buffer.readLong();
        return true;
    }
    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeLong(playerId);
        buffer.writeUTF8(storeName);
        buffer.writeLong(this.itemId);
        buffer.writeBoolean(granted);
        buffer.writeLong(timestamp);
        return true;
    }


    @Override
    public boolean readKey(Recoverable.DataBuffer buffer){
        transactionId = buffer.readUTF8();
        return true;
    }
    @Override
    public boolean writeKey(Recoverable.DataBuffer buffer){
        if(transactionId==null) return false;
        buffer.writeUTF8(transactionId);
        return true;
    }


    public Key key(){
        return new NaturalKey(this.transactionId);
    }


    @Override
    public JsonObject toJson() {
        JsonObject resp = new JsonObject();
        resp.addProperty("playerId",playerId);
        resp.addProperty("storeName",storeName);
        resp.addProperty("transactionId",transactionId);
        resp.addProperty("itemId",Long.toString(itemId));
        resp.addProperty("granted",granted);
        resp.addProperty("transactionDate",TimeUtil.fromUTCMilliseconds(timestamp).format(DateTimeFormatter.ISO_DATE_TIME));
        return resp;
    }
}
