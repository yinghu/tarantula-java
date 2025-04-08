package com.icodesoftware.lmdb.ffm;

public enum TxnMask implements NativeMask {

    TXN_RD_ONLY(0x20000);

    private final int mask;

    TxnMask(int mask){
        this.mask = mask;
    }

    public int mask(){
        return mask;
    }
}
