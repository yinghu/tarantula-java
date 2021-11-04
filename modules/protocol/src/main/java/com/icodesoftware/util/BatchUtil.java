package com.icodesoftware.util;


import java.util.ArrayList;
import java.util.List;

public class BatchUtil {


    public static Batch batch(int length, int batchSize){
        ArrayList<Offset> offsets = new ArrayList<>();
        if(length<=batchSize){
            offsets.add(new Offset((short) 1,0,length));
            return new Batch((short) 1,offsets);
        }
        short batch = 1;
        for(int i=0;i<length;i=i+ batchSize){
            if(i+batchSize<=length){
                offsets.add(new Offset(batch,i,batchSize));
            }
            else{
                offsets.add(new Offset(batch,i,length-i));
            }
            batch++;
        }
        return new Batch((short) (batch-1),offsets);
    }

    public static class Batch{
        public final short size;
        public final List<Offset> offsets;
        public Batch(short size,List<Offset> offsets){
            this.size = size;
            this.offsets = offsets;
        }
    }
    public static class Offset {
        public final short batch;
        public final int offset;
        public final int length;

        public Offset(short batch,int offset, int length) {
            this.batch = batch;
            this.offset = offset;
            this.length = length;
        }
    }

}
