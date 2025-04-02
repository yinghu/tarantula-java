package com.icodesoftware.lmdb.ffm;

public enum MaskFlag {

    //env flags
    ENV_FIXED_MAP(0x01),
    ENV_NO_SUB_DIR(0x4000),
    ENV_NO_SYNC(0x1_0000),
    ENV_READ_ONLY(0x2_0000),
    ENV_WRITE_MAP(0x8_0000),
    EVN_NO_META_SYNC(0x4_0000),
    EVN_MAP_ASYNC(0x10_0000),
    EVN_NO_TLS(0x20_0000),
    EVN_NO_LOCK(0x40_0000),
    EVN_NO_RDA_HEAD(0x80_0000),
    EVN_NO_MEM_INIT(0x100_0000),

    //dbi flags
    DBI_REVERSE_KEY(0x02),
    DBI_DUP_SORT(0x04),
    DBI_INTEGER_KEY(0x08),
    DBI_DUP_FIXED(0x10),
    DBI_INTEGER_DUP(0x20),
    DBI_UNSIGNED_KEY(0x30),
    DBI_REVERSE_DUP(0x40),
    DBI_CREATE(0x4_0000),

    //put flags
    PUT_NO_OVERWRITE(0x10),
    PUT_NO_DUP_DATA(0x20),
    PUT_CURRENT(0x40),
    PUT_RESERVE(0x1_0000),
    PUT_APPEND(0x2_0000),
    PUT_APPEND_DUP(0x4_0000),
    PUT_MULTIPLE(0x8_0000),

    TXN_RD_ONLY(0x20000),

    LINUX_MODE(0644);

    private final int mask;

    MaskFlag(int mask){
        this.mask = mask;
    }

    public int mask(){
        return mask;
    }
}
