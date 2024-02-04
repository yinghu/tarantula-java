package com.icodesoftware.lmdb;

import com.icodesoftware.service.Batchable;

import java.util.ArrayList;
import java.util.List;

public class EdgeValueSet implements Batchable {

    private List<byte[]> key = new ArrayList<>();

    private List<byte[]> data = new ArrayList<>();
    @Override
    public int size() {
        return this.data.size();
    }

    @Override
    public List<byte[]> key() {
        return key;
    }

    @Override
    public List<byte[]> data() {
        return data;
    }

    public void onEdgeValue(byte[] k,byte[] v){
        key.add(k);
        data.add(v);
    }
}
