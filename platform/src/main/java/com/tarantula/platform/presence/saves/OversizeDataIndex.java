package com.tarantula.platform.presence.saves;

import com.icodesoftware.util.RecoverableObject;
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
}
