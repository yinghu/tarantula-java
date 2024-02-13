package com.icodesoftware.lmdb;

import com.icodesoftware.service.Batchable;


public class EdgeValueSet implements Batchable.BatchData {

    protected byte[] key;
    protected byte[] value;

    public EdgeValueSet(){

    }
    public EdgeValueSet(byte[] key,byte[] value){
        this.key = key;
        this.value = value;
    }

    @Override
    public byte[] key() {
        return key;
    }

    @Override
    public byte[] value() {
        return value;
    }


}
