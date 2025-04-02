package com.icodesoftware.lmdb.test;

import com.icodesoftware.lmdb.EnvSetting;
import com.icodesoftware.lmdb.LMDBEnv;
import com.icodesoftware.lmdb.ffm.MaskFlag;
import com.icodesoftware.lmdb.ffm.NativeEnv;
import org.lmdbjava.TargetName;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ForeignAPITest {

    @Test(groups = { "foreign API" })
    public void memorySegmentTest(){
        try(Arena arena = Arena.ofConfined()){
            MemorySegment memorySegment = arena.allocate(0,1);
            Assert.assertTrue(memorySegment.byteSize()==0);
            Assert.assertTrue(memorySegment.address()>0);
        }
        try(Arena arena = Arena.ofConfined()){
            MemorySegment memorySegment = arena.allocate(0);
            Assert.assertTrue(memorySegment.byteSize()==0);
            Assert.assertTrue(memorySegment.address()>0);
        }
        try(Arena arena = Arena.ofConfined()){
            MemorySegment memorySegment = arena.allocate(4*3,4);
            Assert.assertTrue(memorySegment.byteSize()==12);
            memorySegment.fill((byte)0);
            memorySegment.set(ValueLayout.JAVA_INT,0,10);
            memorySegment.set(ValueLayout.JAVA_INT,4,30);
            memorySegment.set(ValueLayout.JAVA_INT,8,300);
            Assert.assertTrue(memorySegment.get(ValueLayout.JAVA_INT,0)==10);
            Assert.assertTrue(memorySegment.get(ValueLayout.JAVA_INT,4)==30);
            Assert.assertTrue(memorySegment.get(ValueLayout.JAVA_INT,8)==300);

            Assert.assertTrue(memorySegment.address()>0);
        }
        try(Arena arena = Arena.ofConfined()){
            MemorySegment memorySegment = arena.allocate(AddressLayout.ADDRESS);
            Assert.assertTrue(memorySegment.byteSize()==8);
            Assert.assertTrue(memorySegment.address()>0);
        }
        try(Arena arena = Arena.ofConfined()){
            MemorySegment memorySegment = arena.allocate(AddressLayout.ADDRESS,2);
            Assert.assertTrue(memorySegment.byteSize()==16);
            Assert.assertTrue(memorySegment.address()>0);
        }

        try(Arena arena = Arena.ofConfined()){
            String javaString = "java string";
            MemorySegment memorySegment = arena.allocateFrom(javaString);
            Assert.assertTrue(memorySegment.byteSize()==javaString.length()+1);
            char j = (char)memorySegment.get(ValueLayout.JAVA_BYTE,0);
            Assert.assertTrue(j=='j');
            char g = (char)memorySegment.get(ValueLayout.JAVA_BYTE,10);
            Assert.assertTrue(g=='g');
            Assert.assertTrue(memorySegment.address()>0);
        }
    }

    @Test(groups = { "foreign API" })
    public void downCallForeignFunctionTest(){
        Throwable throwable = null;
        try(Arena arena = Arena.ofConfined()){
            String javaString = "java string";
            MemorySegment memorySegment = arena.allocateFrom(javaString);
            Assert.assertTrue(memorySegment.byteSize()==javaString.length()+1);
            char j = (char)memorySegment.get(ValueLayout.JAVA_BYTE,0);
            Assert.assertTrue(j=='j');
            char g = (char)memorySegment.get(ValueLayout.JAVA_BYTE,10);
            Assert.assertTrue(g=='g');
            Assert.assertTrue(memorySegment.address()>0);
            Linker linker = Linker.nativeLinker();
            SymbolLookup lookup = linker.defaultLookup();
            MemorySegment functionPointer = lookup.find("strlen").get();
            FunctionDescriptor signature = FunctionDescriptor.of(ValueLayout.JAVA_LONG,ValueLayout.ADDRESS); //size_t strlen(* str);
            MethodHandle caller = linker.downcallHandle(functionPointer,signature);
            long len = (long)caller.invokeExact(memorySegment);
            Assert.assertTrue(len==11);
        }catch (Throwable ex){
            throwable = ex;
        }

        Assert.assertNull(throwable);
    }

    @Test(groups = { "foreign API" })
    public void upCallForeignFunctionTest() {
        Throwable throwable = null;
        try{
            Linker linker = Linker.nativeLinker();
            MethodHandle qSort = linker.downcallHandle(linker.defaultLookup().find("qsort").get(), FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));

            MethodHandle compareHandle = MethodHandles.lookup().findStatic(QSort.class, "compare", MethodType.methodType(int.class, MemorySegment.class, MemorySegment.class));

            FunctionDescriptor qSortCompareDesc = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS.withTargetLayout(ValueLayout.JAVA_INT), ValueLayout.ADDRESS.withTargetLayout(ValueLayout.JAVA_INT));

            MemorySegment upCallStub = linker.upcallStub(compareHandle, qSortCompareDesc, Arena.ofAuto());

            try (Arena arena = Arena.ofConfined()) {
                int[] un = new int[] { 0, 9, 3, 4, 6, 5, 1, 8, 2, 7 };
                MemorySegment array = arena.allocateFrom(ValueLayout.JAVA_INT, un);
                qSort.invoke(array, (long)un.length,ValueLayout.JAVA_INT.byteSize(), upCallStub);
                int[] sorted = array.toArray(ValueLayout.JAVA_INT);
                for(int i=0;i<10;i++){
                    Assert.assertTrue(i==sorted[i]);
                }
            }
        }catch (Throwable ex){
            throwable = ex;
        }
        Assert.assertNull(throwable);
        System.out.println("mask : "+(MaskFlag.ENV_NO_SYNC.mask()|MaskFlag.ENV_WRITE_MAP.mask()));
    }

    public static void main(String[] arg) throws Exception{
        NativeEnv nativeEnv = new NativeEnv();
        nativeEnv.start();
        nativeEnv.shutdown();
    }


    static class QSort{
        public static int compare(MemorySegment c0,MemorySegment c1){
            return Integer.compare(c0.get(ValueLayout.JAVA_INT, 0), c1.get(ValueLayout.JAVA_INT, 0));
        }
    }
}
