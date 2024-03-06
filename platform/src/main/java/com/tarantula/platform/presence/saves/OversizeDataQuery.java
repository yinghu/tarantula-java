package com.tarantula.platform.presence.saves;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;

public class OversizeDataQuery implements RecoverableFactory<BatchedMappingObject> {

    private Recoverable.Key key;
    private String savedKey;

    public OversizeDataQuery(Recoverable.Key key,String savedKey){
        this.key = key;
        this.savedKey = savedKey;
    }

    @Override
    public BatchedMappingObject create() {
        return new BatchedMappingObject();
    }


    @Override
    public String label() {
        return savedKey;
    }

    @Override
    public Recoverable.Key key() {
        return key;
    }
}
