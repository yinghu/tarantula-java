package com.icodesoftware.lmdb.ffm;

public enum CursorMask implements NativeMask{

    MDB_FIRST(0),

    MDB_FIRST_DUP(1),

    MDB_GET_BOTH(2),

    MDB_GET_BOTH_RANGE(3),

    MDB_GET_CURRENT(4),

    MDB_GET_MULTIPLE(5),

    MDB_LAST(6),

    MDB_LAST_DUP(7),

    MDB_NEXT(8),

    MDB_NEXT_DUP(9),

    MDB_NEXT_MULTIPLE(10),

    MDB_NEXT_NODUP(11),

    MDB_PREV(12),

    MDB_PREV_DUP(13),

    MDB_PREV_NODUP(14),

    MDB_SET(15),

    MDB_SET_KEY(16),

    MDB_SET_RANGE(17);

    private final int mask;

    CursorMask(int mask){
        this.mask = mask;
    }

    public int mask(){
        return mask;
    }

}
