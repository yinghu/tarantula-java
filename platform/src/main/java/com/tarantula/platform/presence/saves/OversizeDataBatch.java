package com.tarantula.platform.presence.saves;

import java.io.ByteArrayOutputStream;
import java.util.ArrayDeque;
import java.util.HashMap;

public class OversizeDataBatch {


    public static HashMap<Integer,byte[]> toBatch(byte[] data, int size){
        HashMap<Integer,byte[]> map = new HashMap<>();
        int b = 0;
        for(int i=0;i<data.length;i =i+size){
            int batchEnd = data.length-i<size? data.length : (i+size);
            byte[] chunk = new byte[batchEnd-i];
            for(int j=i ;j<batchEnd;j++){
                chunk[j-i]=data[j];
            }
            map.put(b++,chunk);
        }
        return map;
    }
    public static byte[] fromBatch(HashMap<Integer,BatchedMappingObject> chunks){
        try(ByteArrayOutputStream stream = new ByteArrayOutputStream()){
            for(int i=0;i<chunks.size();i++){
                BatchedMappingObject chunk = chunks.get(i);
                if(chunk!=null) stream.write(chunk.value());
            }
            return stream.toByteArray();
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }



}
