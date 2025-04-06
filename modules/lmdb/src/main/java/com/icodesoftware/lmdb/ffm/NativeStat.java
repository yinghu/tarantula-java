package com.icodesoftware.lmdb.ffm;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class NativeStat {

    protected final AtomicInteger pageSize = new AtomicInteger(0);
    protected final AtomicInteger depth = new AtomicInteger(0);
    protected final AtomicLong branchPages = new AtomicLong(0);
    protected final AtomicLong leafPages = new AtomicLong(0);
    protected final AtomicLong overflowPages = new AtomicLong(0);
    protected final AtomicLong entries = new AtomicLong(0);

    public int pageSize(){
        return pageSize.get();
    }

    public int depth(){
        return depth.get();
    }

    public long branchPages(){
        return overflowPages.get();
    }

    public long leafPages(){
        return overflowPages.get();
    }

    public long overflowPages(){
        return overflowPages.get();
    }

    public long entries(){
        return entries.get();
    }
}
