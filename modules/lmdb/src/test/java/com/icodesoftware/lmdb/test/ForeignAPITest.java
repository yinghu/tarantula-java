package com.icodesoftware.lmdb.test;

import com.icodesoftware.*;
import com.icodesoftware.lmdb.EnvSetting;
import com.icodesoftware.lmdb.ffm.*;
import com.icodesoftware.util.BufferProxy;

import com.icodesoftware.util.SnowflakeKey;
import org.testng.Assert;
import org.testng.annotations.Test;


import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ForeignAPITest extends TestSetup{

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
    public void memoryLayoutTest(){
        ValueLayout intValue = ValueLayout.JAVA_INT;
        ValueLayout doubleValue = ValueLayout.JAVA_DOUBLE;
        Assert.assertEquals(intValue.byteSize(),4);
        Assert.assertEquals(doubleValue.byteSize(),8);

        AddressLayout addressLayout = ValueLayout.ADDRESS;
        Assert.assertEquals(addressLayout.byteSize(),8);

        SequenceLayout sequenceLayout = MemoryLayout.sequenceLayout(10,ValueLayout.JAVA_INT);
        Assert.assertEquals(sequenceLayout.byteSize(),40);

        StructLayout structLayout = MemoryLayout.structLayout(ValueLayout.JAVA_INT.withName("x"), ValueLayout.JAVA_INT.withName("y"));
        Assert.assertEquals(structLayout.byteSize(),8);

        UnionLayout unionLayout = MemoryLayout.unionLayout(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG,ValueLayout.JAVA_INT);
        Assert.assertEquals(unionLayout.byteSize(),8);

        MemoryLayout memoryLayout1 = ValueLayout.JAVA_INT;//4
        MemoryLayout memoryLayout2 = MemoryLayout.structLayout(ValueLayout.JAVA_LONG);//8
        PaddingLayout paddingLayout = MemoryLayout.paddingLayout(4);// 4
        MemoryLayout complexLayout = MemoryLayout.structLayout(memoryLayout1,paddingLayout, memoryLayout2);
        Assert.assertEquals(complexLayout.byteSize(),16);
    }

    @Test(groups = { "foreign API" })
    public void valHandleTest(){
        MemoryLayout pointLayout = MemoryLayout.structLayout(
                ValueLayout.JAVA_INT.withName("x"),
                ValueLayout.JAVA_INT.withName("y")
        );
        VarHandle xHandle = pointLayout.varHandle(MemoryLayout.PathElement.groupElement("x"));
        VarHandle yHandle = pointLayout.varHandle(MemoryLayout.PathElement.groupElement("y"));
        try(Arena arena = Arena.ofConfined()){
            MemorySegment segment = arena.allocate(pointLayout);
            xHandle.set(segment,0,10);
            yHandle.set(segment,0,20);
            int xValue = (int)xHandle.get(segment, 0);
            int yValue = (int)yHandle.get(segment, 0);
            Assert.assertEquals(xValue,10);
            Assert.assertEquals(yValue,20);
        }

        SequenceLayout pointsLayout = MemoryLayout.sequenceLayout(10, pointLayout);
        VarHandle vHandle = pointsLayout.varHandle(MemoryLayout.PathElement.sequenceElement(), MemoryLayout.PathElement.groupElement("x"));
        try(Arena arena = Arena.ofConfined()){
            MemorySegment segment = arena.allocate(pointsLayout);

            for (int i = 0; i < 10; i++) {
                vHandle.set(segment, 0, i, i);
            }

            for (int i = 0; i < 10; i++) {
                Assert.assertEquals(i, vHandle.get(segment, 0, i));
            }
        }

    }

    @Test(groups = { "foreign API" })
    public void zeroLengthMemorySegmentTest(){
        Throwable throwable = null;
        try(Arena arena = Arena.ofConfined()) {

            MemorySegment memorySegment = arena.allocate(ValueLayout.ADDRESS);
            MemorySegment newM = memorySegment.reinterpret(10,arena,mg->{
                Assert.assertEquals(mg.byteSize(),10L);
            });
            newM.set(ValueLayout.JAVA_INT,0,10);
            Assert.assertEquals(memorySegment.byteSize(),8L);
            Assert.assertEquals(newM.byteSize(),10L);
            Assert.assertEquals(newM.get(ValueLayout.JAVA_INT,0),10);


        }catch (Throwable ex){
            ex.printStackTrace();
            throwable = ex;
        }
        Assert.assertNull(throwable);
    }

    @Test(groups = { "foreign API" })
    public void zeroLengthWrapperMemorySegmentTest(){
        Throwable throwable = null;
        try(Arena arena = Arena.ofConfined()){
            StructLayout structLayout = MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withName("mv_size"),AddressLayout.ADDRESS.withName("mv_data"));
            MemorySegment pointer = arena.allocate(structLayout);
            VarHandle vSize = structLayout.varHandle(MemoryLayout.PathElement.groupElement("mv_size"));
            vSize.set(pointer,0,100);
            VarHandle vData = structLayout.varHandle(MemoryLayout.PathElement.groupElement("mv_data"));
            MemorySegment data = arena.allocateFrom("test", StandardCharsets.US_ASCII);
            vData.set(pointer,0,data);

        }catch (Throwable ex){
            ex.printStackTrace();
            throwable = ex;
        }
        Assert.assertNull(throwable);
    }

    @Test(groups = { "foreign API" })
    public void memorySegmentWriteTest(){
        Throwable throwable = null;
        try(Arena arena = Arena.ofConfined()){
            StructLayout structLayout = MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withName("mv_size"),AddressLayout.ADDRESS.withName("mv_data"));
            MemorySegment pointer = arena.allocate(structLayout);
            VarHandle vSize = structLayout.varHandle(MemoryLayout.PathElement.groupElement("mv_size"));
            vSize.set(pointer,0,100);
            VarHandle vData = structLayout.varHandle(MemoryLayout.PathElement.groupElement("mv_data"));
            MemorySegment data = arena.allocate(MemoryLayout.sequenceLayout(1780,ValueLayout.JAVA_BYTE));
            ByteBuffer buffer = data.asByteBuffer();
            TestObject testObject = new TestObject("TYPE","NAME");
            testObject.write(BufferProxy.buffer(buffer));
            buffer.flip();
            vData.set(pointer,0,data);

        }catch (Throwable ex){
            ex.printStackTrace();
            throwable = ex;
        }
        Assert.assertNull(throwable);
    }
    private static void delete(DataStore dataStore,DataStore index){
        TestObject accessIndex = new TestObject("tester6","testName");
        accessIndex.ownerKey(SnowflakeKey.from(100));
        accessIndex.label("tester");
        accessIndex.onEdge(true);
        System.out.println(dataStore.create(accessIndex));
        TestObject load = new TestObject();
        load.distributionId(accessIndex.distributionId());
        dataStore.load(load);
        System.out.println(load.type+" ; "+load.name);
        System.out.println(dataStore.delete(accessIndex));
        TestObject delete = new TestObject();
        load.distributionId(accessIndex.distributionId());
        System.out.println(dataStore.load(delete));

        TestObject to = new TestObject("to100","to200");
        System.out.println(dataStore.create(to));
        dataStore.backup().unset((k,v)->{
            to.key().write(k);
            return true;
        });
        TestObject del = new TestObject();
        del.distributionId(to.distributionId());
        System.out.println(dataStore.load(del));

        for(int i=0;i<10;i++){
            TestObject abc = new TestObject("tester6","testName");
            abc.ownerKey(SnowflakeKey.from(200));
            abc.label("fork");
            abc.onEdge(true);
            System.out.println(dataStore.create(abc));
        }
        System.out.println("TX : "+dataStore.list(new TestObjectQuery(200,"fork")).size());
        dataStore.deleteEdge(SnowflakeKey.from(200),"fork");
        System.out.println("TX : "+dataStore.list(new TestObjectQuery(200,"fork")).size());
    }

    private static void create(DataStore dataStore,DataStore index){
        TestObject accessIndex = new TestObject("tester6","testName");
        accessIndex.ownerKey(SnowflakeKey.from(100));
        accessIndex.label("tester");
        accessIndex.onEdge(true);
        System.out.println(dataStore.create(accessIndex));
        System.out.println(accessIndex.distributionId());
        System.out.println(dataStore.list(new TestObjectQuery(100,"tester")).size());
        dataStore.createEdge(accessIndex,"link");
        System.out.println(dataStore.list(new TestObjectQuery(100,"link")).size());
        dataStore.deleteEdge(SnowflakeKey.from(100),"link");
        System.out.println(dataStore.list(new TestObjectQuery(100,"link")).size());
        dataStore.deleteEdge(SnowflakeKey.from(100),accessIndex.key(),"tester");
        System.out.println(dataStore.list(new TestObjectQuery(100,"tester")).size());
        System.out.println(dataStore.backup().setEdge("ace_me",(k,v)->{
            SnowflakeKey.from(100).write(k);
            accessIndex.key().write(v);
            return true;
        }));
        System.out.println(dataStore.list(new TestObjectQuery(100,"ace_me")).size());
        index.createEdge(accessIndex,"delete_me");
        int[] ct = {0};
        index.backup().forEachEdgeKey(SnowflakeKey.from(100),"delete_me",(k,v)->{
            ct[0]++;
            return true;
        });
        System.out.println("delete_me : "+ct[0]);
        index.backup().unsetEdge("delete_me",(k,v)->{
            SnowflakeKey.from(100).write(k);
            accessIndex.key().write(v);
            return true;
        },false);
        ct[0]=0;
        index.backup().forEachEdgeKey(SnowflakeKey.from(100),"delete_me",(k,v)->{
            ct[0]++;
            return true;
        });
        System.out.println("delete_me : "+ct[0]);
    }

    private static void createIfAbsent(DataStore dataStore,DataStore index){
        TestAccessIndex testAccessIndex = new TestAccessIndex(UUID.randomUUID().toString());
        testAccessIndex.referenceId = 10;
        testAccessIndex.group = "provider";
        testAccessIndex.distributionId(100);
        testAccessIndex.ownerKey(SnowflakeKey.from(9090));
        System.out.println(dataStore.createIfAbsent(testAccessIndex, false));
        boolean loaded = dataStore.backup().get(testAccessIndex.key(),(k,v)->{
            System.out.println(k.remaining());
            System.out.println(v.remaining());
            index.backup().set((xk,xv)->{
                xk.write(k);
                xv.write(v);
                return true;
            });
            return true;
        });
        System.out.println(loaded);
        index.backup().get(testAccessIndex.key(),(k,v)->{
            Recoverable.DataHeader h = v.readHeader();
            System.out.println(h.factoryId()+" : "+h.classId()+" ; "+h.revision());
            TestAccessIndex cp = new TestAccessIndex();
            cp.readKey(k);
            cp.read(v);
            return true;
        });
        dataStore.backup().forEach((k,v)->{
            //System.out.println(k.remaining());
            //System.out.println(v.remaining());
            return true;
        });
        dataStore.list(new TestAccessQuery(9090,"access"),ta->{
            //System.out.println(">>>>>>>>>>>>"+ta.group);
            return true;
        });
        System.out.println("SZ : "+dataStore.list(new TestAccessQuery(9090,"access")).size());
        TestAccessIndex load = new TestAccessIndex(testAccessIndex.owner());
        dataStore.load(load);
        System.out.println(load.group);
        load.group = "provider1";
        dataStore.update(load);
        TestAccessIndex load1 = new TestAccessIndex(testAccessIndex.owner());
        dataStore.load(load1);
        System.out.println(load1.group);

        TestAccessIndex load2 = new TestAccessIndex(testAccessIndex.owner());
        dataStore.createIfAbsent(load2,true);
        System.out.println(load2.group);
        System.out.println(dataStore.delete(load2));
        System.out.println(dataStore.load(load1));
    }


    public static void main(String[] arg) throws Exception{
        try{
            NativeDataStoreProvider nativeDataStoreProvider = new NativeDataStoreProvider();
            TestMapStoreListener mapStoreListener = new TestMapStoreListener(nativeDataStoreProvider);
            mapStoreListener.verifier = (tid)->{
                List<Transaction.Log> logs = mapStoreListener.transactionLogManager.committed(Distributable.DATA_SCOPE,tid);
                System.out.println("TID : "+tid+" : " +logs.size());
            };
            nativeDataStoreProvider.registerMapStoreListener(Distributable.DATA_SCOPE,mapStoreListener);
            Map<String,Object> config = new HashMap<>();
            String baseDir = "/var/lmdb";
            config.put(EnvSetting.data,EnvSetting.setting(Distributable.DATA_SCOPE,baseDir,10));
            config.put(EnvSetting.integration,EnvSetting.setting(Distributable.INTEGRATION_SCOPE,baseDir,10));
            config.put(EnvSetting.index,EnvSetting.setting(Distributable.INDEX_SCOPE,baseDir,10));
            config.put(EnvSetting.log,EnvSetting.setting(Distributable.LOG_SCOPE,baseDir,10));
            config.put(EnvSetting.local,EnvSetting.setting(Distributable.LOCAL_SCOPE,baseDir,10));
            nativeDataStoreProvider.configure(config);
            nativeDataStoreProvider.start();
            DataStore nativeDataStore = nativeDataStoreProvider.createDataStore("access");
            DataStore index = nativeDataStoreProvider.createKeyIndexDataStore("access_index");
            createIfAbsent(nativeDataStore,index);
            create(nativeDataStore,index);
            delete(nativeDataStore,index);
            //TestObject accessIndex = new TestObject("tester6","testName");
            //System.out.println(nativeDataStore.create(accessIndex));
            //System.out.println(accessIndex.distributionId());
            //accessIndex.ownerKey(SnowflakeKey.from(100));

            nativeDataStoreProvider.shutdown();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }


    static class QSort{
        public static int compare(MemorySegment c0,MemorySegment c1){
            return Integer.compare(c0.get(ValueLayout.JAVA_INT, 0), c1.get(ValueLayout.JAVA_INT, 0));
        }
    }
}
