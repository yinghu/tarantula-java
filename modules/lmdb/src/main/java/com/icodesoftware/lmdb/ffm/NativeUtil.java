package com.icodesoftware.lmdb.ffm;

import com.icodesoftware.Recoverable;

import java.lang.foreign.*;
import java.lang.invoke.VarHandle;

public class NativeUtil {

    private NativeUtil(){}

    public static MemorySegment mdbVal(Arena arena, Recoverable.DataBuffer value){
        long len = value.remaining()+1;
        StructLayout struct = MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withName("mv_size"),ValueLayout.ADDRESS.withName("mv_data"));
        MemorySegment pointer = arena.allocate(struct);
        VarHandle vSize = struct.varHandle(MemoryLayout.PathElement.groupElement("mv_size"));
        vSize.set(pointer,0,len);
        VarHandle vData = struct.varHandle(MemoryLayout.PathElement.groupElement("mv_data"));
        MemorySegment sequence = arena.allocate(MemoryLayout.sequenceLayout(len,ValueLayout.JAVA_BYTE));
        long offset = 0;
        while (value.hasRemaining()){
            sequence.set(ValueLayout.JAVA_BYTE,offset++,value.readByte());
        }
        sequence.set(ValueLayout.JAVA_BYTE,offset,(byte) '\0');
        vData.set(pointer,0,sequence);
        return pointer;
    }

    public static MemorySegment mdbVal(Arena arena){
        StructLayout struct = MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withName("mv_size"),ValueLayout.ADDRESS.withName("mv_data"));
        MemorySegment pointer = arena.allocate(struct);
        return pointer;
    }

    public static MemorySegment mdbStat(Arena arena){
        StructLayout layout = MemoryLayout.structLayout(ValueLayout.JAVA_INT.withName("ms_psize"),ValueLayout.JAVA_INT.withName("ms_depth"),
                ValueLayout.JAVA_LONG.withName("ms_branch_pages"),ValueLayout.JAVA_LONG.withName("ms_leaf_pages"),ValueLayout.JAVA_LONG.withName("ms_overflow_pages"),ValueLayout.JAVA_LONG.withName("ms_entries"));
        MemorySegment memorySegment = arena.allocate(layout);
        return memorySegment;
    }

    public static MemorySegment mdbInfo(Arena arena){
        StructLayout layout = MemoryLayout.structLayout(AddressLayout.ADDRESS.withName("me_mapaddr"),ValueLayout.JAVA_LONG.withName("me_mapsize")
                ,ValueLayout.JAVA_LONG.withName("me_last_pgno"),ValueLayout.JAVA_LONG.withName("me_last_txnid"),
                ValueLayout.JAVA_INT.withName("me_max_readers"),ValueLayout.JAVA_INT.withName("me_numreaders"));
        MemorySegment memorySegment = arena.allocate(layout);
        return memorySegment;
    }

    public static MemorySegment mdbPointer(Arena arena){
        return arena.allocate(AddressLayout.ADDRESS);
    }
}
