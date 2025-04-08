package com.icodesoftware.lmdb.ffm;

public enum DbiMask implements NativeMask{

    DBI_REVERSE_KEY(0x02),
    DBI_DUP_SORT(0x04),
    DBI_INTEGER_KEY(0x08),
    DBI_DUP_FIXED(0x10),
    DBI_INTEGER_DUP(0x20),
    DBI_UNSIGNED_KEY(0x30),
    DBI_REVERSE_DUP(0x40),
    DBI_CREATE(0x4_0000);

    private int mask;

    DbiMask(int mask){
        this.mask = mask;
    }

    @Override
    public int mask() {
        return mask;
    }
}
