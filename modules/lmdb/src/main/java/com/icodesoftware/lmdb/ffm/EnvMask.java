package com.icodesoftware.lmdb.ffm;

public enum EnvMask implements NativeMask{

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

    ACCESS_MODE(0644);

    private int mask;

    EnvMask(int mask){
        this.mask = mask;
    }

    @Override
    public int mask() {
        return mask;
    }
}
