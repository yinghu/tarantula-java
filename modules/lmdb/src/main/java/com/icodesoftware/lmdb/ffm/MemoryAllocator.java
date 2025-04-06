package com.icodesoftware.lmdb.ffm;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public interface MemoryAllocator {
    MemorySegment onAllocate(Arena arena);
}
