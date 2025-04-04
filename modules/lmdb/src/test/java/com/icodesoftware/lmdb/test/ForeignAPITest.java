package com.icodesoftware.lmdb.test;

import com.icodesoftware.Recoverable;
import com.icodesoftware.lmdb.ffm.NativeEnv;
import com.icodesoftware.util.BufferProxy;

import org.testng.Assert;
import org.testng.annotations.Test;


import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;


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
        //System.out.println("mask : "+(MaskFlag.ENV_NO_SYNC.mask()|MaskFlag.ENV_WRITE_MAP.mask()));
    }

    @Test(groups = { "foreign API" })
    public void bufferOnMemorySegmentTest(){
        try(Arena arena = Arena.ofConfined()) {
            MemorySegment memorySegment = arena.allocate(200);
            Recoverable.DataBuffer buffer = BufferProxy.buffer(memorySegment.asByteBuffer());
            buffer.writeInt(100).writeInt(200).flip();
            int x0 = memorySegment.get(ValueLayout.JAVA_INT,0);
            int x1 = memorySegment.get(ValueLayout.JAVA_INT,4);
            Assert.assertEquals(x0,100);
            Assert.assertEquals(x1,200);
        }
    }

    @Test(groups = { "foreign API" })
    public void structOnMemorySegmentTest(){
        Throwable throwable = null;
        try(Arena arena = Arena.ofConfined()) {
            MemoryLayout groupLayout = MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withName("mv_size"),ValueLayout.ADDRESS.withName("mv_data"));
            MemorySegment key = arena.allocateFrom("test");
            MemorySegment memorySegment = arena.allocate(groupLayout);
            memorySegment.set(ValueLayout.JAVA_LONG,0,10);
            memorySegment.set(ValueLayout.ADDRESS,8,key);
        }catch (Throwable ex){
            throwable = ex;
        }
        Assert.assertNull(throwable);
    }

    @Test(groups = { "foreign API" })
    public void structNestedArrayOnMemorySegmentTest(){
        Throwable throwable = null;
        //struct{ size_t mv_size,byte[] mv_data}
        try(Arena arena = Arena.ofConfined()) {
            SequenceLayout arr = MemoryLayout.sequenceLayout(11,ValueLayout.JAVA_BYTE).withName("mv_data");
            MemoryLayout groupLayout = MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withName("mv_size"),arr);
            MemorySegment memorySegment = arena.allocate(groupLayout);
            memorySegment.set(ValueLayout.JAVA_LONG,groupLayout.byteOffset(MemoryLayout.PathElement.groupElement("mv_size")),11);
            long offset = groupLayout.byteOffset(MemoryLayout.PathElement.groupElement("mv_data"));
            for(int i=0;i<10;i++){
                memorySegment.set(ValueLayout.JAVA_BYTE,offset+i,(byte)'C');
            }
            for(int i=0;i<10;i++){
                Assert.assertEquals((char)memorySegment.get(ValueLayout.JAVA_BYTE,offset+i),'C');
            }
            Assert.assertEquals(memorySegment.get(ValueLayout.JAVA_LONG,groupLayout.byteOffset(MemoryLayout.PathElement.groupElement("mv_size"))),11);
            Assert.assertEquals(offset,8);

        }catch (Throwable ex){
            throwable = ex;
        }
        Assert.assertNull(throwable);
    }

    @Test(groups = { "foreign API" })
    public void ValHandeMemorySegmentTest(){
        Throwable throwable = null;
        try(Arena arena = Arena.ofConfined()) {
            MemoryLayout groupLayout = MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withName("mv_size"),MemoryLayout.paddingLayout(8),
                    MemoryLayout.sequenceLayout(10,ValueLayout.JAVA_INT).withName("mv_data"));
            MemorySegment memorySegment = arena.allocate(groupLayout);
            VarHandle vSize = groupLayout.varHandle(MemoryLayout.PathElement.groupElement("mv_size"));
            vSize.set(memorySegment,0,100);
            Assert.assertEquals(vSize.get(memorySegment,0),100L);
            for(int i=0;i<10;i++){
                VarHandle vData = groupLayout.varHandle(MemoryLayout.PathElement.groupElement("mv_data"), MemoryLayout.PathElement.sequenceElement(i));
                vData.set(memorySegment,0,12);
                Assert.assertEquals(vData.get(memorySegment,0),12);
            }
            Assert.assertEquals(vSize.get(memorySegment,0),100L);
            for(int i=0;i<10;i++){
                VarHandle vData = groupLayout.varHandle(MemoryLayout.PathElement.groupElement("mv_data"), MemoryLayout.PathElement.sequenceElement(i));
                Assert.assertEquals(vData.get(memorySegment,0),12);
            }
        }catch (Throwable ex){
            throwable = ex;
        }
        Assert.assertNull(throwable);
    }
    @Test(groups = { "foreign API" })
    public void sliceMemorySegmentTest(){
        Throwable throwable = null;
        try(Arena arena = Arena.ofConfined()) {

            MemorySegment memorySegment = arena.allocate(12);
            
            VarHandle vData = ValueLayout.JAVA_INT.varHandle();
            vData.set(memorySegment.asSlice(0,4),0,100); //0-3
            vData.set(memorySegment.asSlice(4,4),0,400); //4-7
            vData.set(memorySegment.asSlice(8,4),0,500); //8-11

            Assert.assertEquals(vData.get(memorySegment,0),100);
            Assert.assertEquals(vData.get(memorySegment,4),400);
            Assert.assertEquals(vData.get(memorySegment,8),500);


        }catch (Throwable ex){
            throwable = ex;
        }
        Assert.assertNull(throwable);
    }
    @Test(groups = { "foreign API" })
    public void zeroLengthMemorySegmentTest(){
        Throwable throwable = null;
        try(Arena arena = Arena.ofConfined()) {
            MemoryLayout groupLayout = MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withName("mv_size"),ValueLayout.ADDRESS.withName("mv_data"));
            MemorySegment memorySegment = arena.allocate(groupLayout);
            System.out.println(memorySegment.byteSize());
            //VarHandle vData = groupLayout.varHandle(MemoryLayout.PathElement.groupElement("mv_data"));
            //MemorySegment data = (MemorySegment)vData.get(memorySegment,1);
            //MemorySegment data = memorySegment.get(ValueLayout.ADDRESS,0);
            //data.reinterpret(10);
            //System.out.println(data.address());
            //data.set(ValueLayout.JAVA_INT,0,100);

        }catch (Throwable ex){
            throwable = ex;
        }
        Assert.assertNull(throwable);
    }

    public static void main(String[] arg) throws Exception{
        NativeEnv nativeEnv = new NativeEnv();
        nativeEnv.start();
        //nativeEnv.createDbi("test100");
        //nativeEnv.createDbi("test2");
        //nativeEnv.createDbi("test3");
        nativeEnv.putTest("test_mill","key1123","value1121");
        nativeEnv.shutdown();
    }


    static class QSort{
        public static int compare(MemorySegment c0,MemorySegment c1){
            return Integer.compare(c0.get(ValueLayout.JAVA_INT, 0), c1.get(ValueLayout.JAVA_INT, 0));
        }
    }
}
