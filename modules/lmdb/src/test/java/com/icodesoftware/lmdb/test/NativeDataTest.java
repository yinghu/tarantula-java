package com.icodesoftware.lmdb.test;

import com.beust.ah.A;
import com.icodesoftware.Recoverable;
import com.icodesoftware.lmdb.EnvSetting;
import com.icodesoftware.lmdb.ffm.NativeData;
import com.icodesoftware.util.BufferProxy;
import com.icodesoftware.util.LocalHeader;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.foreign.Arena;


public class NativeDataTest {

    @Test(groups = { "Native Data" })
    public void valInTest(){
        try(Arena arena = Arena.ofConfined()){
            NativeData.InVal in = NativeData.in(arena, EnvSetting.VALUE_SIZE);
            TestObject testObject = new TestObject("TYPE","NAME");
            Recoverable.DataBuffer aBuffer = in.write(buffer -> {
                buffer.writeHeader(LocalHeader.create(100,10,1l));
                testObject.write(buffer);
            });

            //direct
            Recoverable.DataHeader h = aBuffer.readHeader();
            Assert.assertEquals(h.revision(),1L);
            TestObject to = new TestObject();
            to.read(aBuffer);
            Assert.assertEquals(to.name,testObject.name);
            Assert.assertEquals(to.type,testObject.type);
            aBuffer.rewind();

            //copy
            Recoverable.DataBuffer copy = BufferProxy.copy(aBuffer.src());
            Recoverable.DataHeader dh = copy.readHeader();
            Assert.assertEquals(dh.revision(),1L);
            Assert.assertEquals(dh.factoryId(),100);
            Assert.assertEquals(dh.classId(),10);
            TestObject tox = new TestObject();
            tox.read(copy);
            Assert.assertEquals(tox.name,testObject.name);
            Assert.assertEquals(tox.type,testObject.type);

            //transfer
            aBuffer.rewind();
            Recoverable.DataBuffer transfer = BufferProxy.buffer(EnvSetting.VALUE_SIZE,false);
            BufferProxy.transfer(aBuffer,transfer);
            transfer.flip();
            Recoverable.DataHeader fh = transfer.readHeader();
            Assert.assertEquals(fh.revision(),1L);
            Assert.assertEquals(fh.factoryId(),100);
            Assert.assertEquals(fh.classId(),10);
            TestObject toy = new TestObject();
            toy.read(transfer);
            Assert.assertEquals(toy.name,testObject.name);
            Assert.assertEquals(toy.type,testObject.type);

        }
    }
}
