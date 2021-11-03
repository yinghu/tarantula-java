package com.icodesoftware.test;

import com.icodesoftware.protocol.MessageBuffer;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


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
}
