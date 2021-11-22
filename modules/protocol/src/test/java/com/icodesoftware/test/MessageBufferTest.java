package com.icodesoftware.test;

import com.icodesoftware.protocol.MessageBuffer;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.reflect.AnnotatedArrayType;


public class MessageBufferTest {

    @BeforeClass
    public void setUp() {
    }

    @Test(groups = { "message buffer" })
    public void ackMessageBufferTest() {
        MessageBuffer messageBuffer = new MessageBuffer();
        messageBuffer.writeHeader(new MessageBuffer.MessageHeader());
        for(int i=0;i<10;i++){
            messageBuffer.writeHeader(new MessageBuffer.MessageHeader());
        }
        messageBuffer.flip();
        Assert.assertEquals(messageBuffer.toArray().length,25*11);
        messageBuffer.flip();
        Assert.assertEquals(messageBuffer.toArray().length,25*11);
        messageBuffer.flip();
        Assert.assertEquals(messageBuffer.toArray().length,25*11);
    }
    @Test(groups = { "message buffer" })
    public void resetMessageBufferTest(){
        MessageBuffer messageBuffer = new MessageBuffer();
        messageBuffer.writeHeader(new MessageBuffer.MessageHeader());
        messageBuffer.flip();
        Assert.assertEquals(messageBuffer.toArray().length,25);
        messageBuffer.reset("123".getBytes());
        messageBuffer.writeHeader(new MessageBuffer.MessageHeader());
        messageBuffer.flip();
        Assert.assertEquals(messageBuffer.toArray().length,28);
        messageBuffer.reset();
        messageBuffer.writeHeader(new MessageBuffer.MessageHeader());
        messageBuffer.flip();
        Assert.assertEquals(messageBuffer.toArray().length,25);

    }
    @Test(groups = { "message buffer" })
    public void readMessageBufferTest(){
        MessageBuffer messageBuffer = new MessageBuffer();
        messageBuffer.writeHeader(new MessageBuffer.MessageHeader());
        messageBuffer.writeHeader(new MessageBuffer.MessageHeader());
        messageBuffer.flip();
        messageBuffer.readHeader();
        messageBuffer.rewind();
        Assert.assertEquals(messageBuffer.toArray().length,50);
    }
    @Test(groups = { "message buffer" })
    public void bitwiseMessageBufferTest(){
        byte b = (byte)0x00;
        Assert.assertEquals(b,0);
        Assert.assertEquals(b&1,0);
        Assert.assertEquals(b&2,0);
        Assert.assertEquals(b&4,0);

        int x = b|0x04; //0000,0100 -> broadcasting
        Assert.assertEquals(x,4);
        Assert.assertEquals(x&1,0);
        Assert.assertEquals(x&2,0);
        Assert.assertEquals(x&4,4);

        int y = x|0x01; //0000,0001 -> ack
        Assert.assertEquals(y,5);
        Assert.assertEquals(y&1,1);
        Assert.assertEquals(y&2,0);
        Assert.assertEquals(y&4,4);

        int z = y|0x02; //0000,0010 -> encrypted
        Assert.assertEquals(z,7);
        Assert.assertEquals(z&1,1);
        Assert.assertEquals(z&2,2);
        Assert.assertEquals(z&4,4);
    }
}
