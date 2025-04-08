package com.icodesoftware.lmdb.ffm;

public enum PutMask implements NativeMask {

    //put flags
    PUT_NO_OVERWRITE(0x10),
    PUT_NO_DUP_DATA(0x20),
    PUT_CURRENT(0x40),
    PUT_RESERVE(0x1_0000),
    PUT_APPEND(0x2_0000),
    PUT_APPEND_DUP(0x4_0000),
    PUT_MULTIPLE(0x8_0000);


    private final int mask;

    PutMask(int mask){
        this.mask = mask;
    }

    public int mask(){
        return mask;
    }
}
