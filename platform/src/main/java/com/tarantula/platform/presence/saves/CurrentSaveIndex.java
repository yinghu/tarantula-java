package com.tarantula.platform.presence.saves;

import com.icodesoftware.Session;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.presence.PresencePortableRegistry;

public class CurrentSaveIndex extends RecoverableObject {

    //session  <> save Id mapping for current save selection or default save

    public long saveId;

    public CurrentSaveIndex(){

    }

    public CurrentSaveIndex(Session session){
        this.distributionId = session.stub();
    }

    public boolean read(DataBuffer buffer){
        this.saveId = buffer.readLong();
        return true;
    }
    public boolean write(DataBuffer buffer) {
        buffer.writeLong(saveId);
        return true;
    }
    @Override
    public int getClassId() {
        return PresencePortableRegistry.CURRENT_SAVE_INDEX_CID;
    }

    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }

}
