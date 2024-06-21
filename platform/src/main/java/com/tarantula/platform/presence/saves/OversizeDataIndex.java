package com.tarantula.platform.presence.saves;

import com.icodesoftware.DataStore;
import com.icodesoftware.Session;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.SnowflakeKey;
import com.tarantula.platform.presence.PresencePortableRegistry;

public class OversizeDataIndex extends RecoverableObject {

    public static final String LABEL = "oversize_data_index";

    public int batch;
    public String saveKey;
    public OversizeDataIndex(){
        this.onEdge = true;
        this.label = LABEL;
    }
    public OversizeDataIndex(String key){
        this();
        this.saveKey = key;
    }
    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PresencePortableRegistry.OVERSIZE_DATA_INDEX_CID;
    }

    @Override
    public boolean write(DataBuffer buffer){
        buffer.writeUTF8(saveKey);
        buffer.writeInt(batch);
        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        saveKey = buffer.readUTF8();
        batch = buffer.readInt();
        return true;
    }

    public static OversizeDataIndex load(DataStore dataStore,Session session){
        OversizeDataIndex[] indexed = {null};
        String key = session.name();
        dataStore.list(new OversizeDataIndexQuery(SnowflakeKey.from(session.distributionId())),(index)->{
            if(!index.saveKey.equals(key)) return true;
            indexed[0] = index;
            indexed[0].dataStore(dataStore);
            return false;
        });
        return indexed[0];
    }

    public static OversizeDataIndex createIfNotExisted(DataStore dataStore, Session session,int chunkSize){
        OversizeDataIndex[] indexed = {null};
        String key = session.name();
        dataStore.list(new OversizeDataIndexQuery(SnowflakeKey.from(session.distributionId())),(index)->{
            if(!index.saveKey.equals(key)) return true;
            indexed[0] = index;
            indexed[0].dataStore(dataStore);
            return false;
        });
        if(indexed[0]!=null){
            indexed[0].batch = chunkSize;
            dataStore.update(indexed[0]);
            return indexed[0];
        }

        indexed[0] = new OversizeDataIndex(key);
        indexed[0].saveKey = key;
        indexed[0].batch = chunkSize;
        indexed[0].ownerKey(SnowflakeKey.from(session.distributionId()));
        dataStore.create(indexed[0]);
        indexed[0].dataStore(dataStore);
        return indexed[0];
    }
}
