package com.icodesoftware.test;

import com.icodesoftware.protocol.MessageBuffer;
import com.icodesoftware.util.CipherUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.crypto.Cipher;
import java.lang.reflect.AnnotatedArrayType;
import java.security.Key;
import java.security.SecureRandom;


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
        Assert.assertEquals(messageBuffer.toArray().length,MessageBuffer.HEADER_SIZE*11);
        messageBuffer.flip();
        Assert.assertEquals(messageBuffer.toArray().length,MessageBuffer.HEADER_SIZE*11);
        messageBuffer.flip();
        Assert.assertEquals(messageBuffer.toArray().length,MessageBuffer.HEADER_SIZE*11);
    }
    @Test(groups = { "message buffer" })
    public void resetMessageBufferTest(){
        MessageBuffer messageBuffer = new MessageBuffer();
        messageBuffer.writeHeader(new MessageBuffer.MessageHeader());
        messageBuffer.flip();
        Assert.assertEquals(messageBuffer.toArray().length,MessageBuffer.HEADER_SIZE);
        messageBuffer.reset("123".getBytes());
        messageBuffer.writeHeader(new MessageBuffer.MessageHeader());
        messageBuffer.flip();
        Assert.assertEquals(messageBuffer.toArray().length,MessageBuffer.HEADER_SIZE+3);
        messageBuffer.reset();
        messageBuffer.writeHeader(new MessageBuffer.MessageHeader());
        messageBuffer.flip();
        Assert.assertEquals(messageBuffer.toArray().length,MessageBuffer.HEADER_SIZE);

    }
    @Test(groups = { "message buffer" })
    public void readMessageBufferTest(){
        MessageBuffer messageBuffer = new MessageBuffer();
        messageBuffer.writeHeader(new MessageBuffer.MessageHeader());
        messageBuffer.writeHeader(new MessageBuffer.MessageHeader());
        messageBuffer.flip();
        messageBuffer.readHeader();
        messageBuffer.rewind();
        Assert.assertEquals(messageBuffer.toArray().length,MessageBuffer.HEADER_SIZE*2);
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

        int bits = 1;
        bits = bits|2;
        bits = bits|4;
        Assert.assertEquals(7,bits);
        Assert.assertEquals(1,bits&1);
        Assert.assertEquals(2,bits&2);
        Assert.assertEquals(4,bits&4);

        MessageBuffer.MessageHeader messageHeader = new MessageBuffer.MessageHeader();
        messageHeader.encrypted = true;
        messageHeader.ack = true;
        messageHeader.broadcasting = true;
        MessageBuffer messageBuffer = new MessageBuffer();
        messageBuffer.writeHeader(messageHeader);
        messageBuffer.flip();
        MessageBuffer.MessageHeader _h = messageBuffer.readHeader();
        Assert.assertEquals(true,_h.ack);
        Assert.assertEquals(true,_h.encrypted);
        Assert.assertEquals(true,_h.broadcasting);

    }
    @Test(groups = { "message buffer" })
    public void bitwiseMessageHeaderTest1(){

        MessageBuffer.MessageHeader messageHeader = new MessageBuffer.MessageHeader();
        messageHeader.encrypted = true;
        messageHeader.ack = false;
        messageHeader.broadcasting = true;
        MessageBuffer messageBuffer = new MessageBuffer();
        messageBuffer.writeHeader(messageHeader);
        messageBuffer.flip();
        MessageBuffer.MessageHeader _h = messageBuffer.readHeader();
        Assert.assertEquals(false,_h.ack);
        Assert.assertEquals(true,_h.encrypted);
        Assert.assertEquals(true,_h.broadcasting);
    }

    @Test(groups = { "message buffer" })
    public void bitwiseMessageHeaderTest2(){

        MessageBuffer.MessageHeader messageHeader = new MessageBuffer.MessageHeader();
        messageHeader.encrypted = false;
        messageHeader.ack = false;
        messageHeader.broadcasting = true;
        MessageBuffer messageBuffer = new MessageBuffer();
        messageBuffer.writeHeader(messageHeader);
        messageBuffer.flip();
        MessageBuffer.MessageHeader _h = messageBuffer.readHeader();
        Assert.assertEquals(false,_h.ack);
        Assert.assertEquals(false,_h.encrypted);
        Assert.assertEquals(true,_h.broadcasting);
    }

    @Test(groups = { "message buffer" })
    public void bitwiseMessageHeaderTest3(){

        MessageBuffer.MessageHeader messageHeader = new MessageBuffer.MessageHeader();
        messageHeader.encrypted = false;
        messageHeader.ack = false;
        messageHeader.broadcasting = false;
        MessageBuffer messageBuffer = new MessageBuffer();
        messageBuffer.writeHeader(messageHeader);
        messageBuffer.flip();
        MessageBuffer.MessageHeader _h = messageBuffer.readHeader();
        Assert.assertEquals(false,_h.ack);
        Assert.assertEquals(false,_h.encrypted);
        Assert.assertEquals(false,_h.broadcasting);
    }

    @Test(groups = { "message buffer" })
    public void messageHeaderTestCipher(){
        try {
            byte[] key = new byte[16];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(key);
            Cipher en = CipherUtil.encrypt(key);
            Cipher de = CipherUtil.decrypt(key);
            MessageBuffer.MessageHeader messageHeader = new MessageBuffer.MessageHeader();
            messageHeader.encrypted = true;
            messageHeader.ack = false;
            messageHeader.broadcasting = false;
            MessageBuffer messageBuffer = new MessageBuffer();
            messageBuffer.writeHeader(messageHeader);
            messageBuffer.writeUTF8("test1");
            messageBuffer.flip();
            messageBuffer.readHeader();
            byte[] out = en.doFinal(messageBuffer.readPayload());
            messageBuffer.reset();
            messageBuffer.writeHeader(messageHeader);
            messageBuffer.writePayload(out);
            messageBuffer.flip();
            messageBuffer.readHeader();
            byte[] ex = de.doFinal(messageBuffer.readPayload());
            messageBuffer.reset();
            messageBuffer.writeHeader(messageHeader);
            messageBuffer.writePayload(ex);
            messageBuffer.flip();
            messageBuffer.readHeader();
            String op = messageBuffer.readUTF8();
            Assert.assertEquals("test1",op);
        }catch (Exception ex){
            ex.printStackTrace();
            Assert.assertEquals(1,2);
        }
    }
}
