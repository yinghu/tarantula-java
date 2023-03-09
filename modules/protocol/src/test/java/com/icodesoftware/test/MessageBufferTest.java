package com.icodesoftware.test;

import com.icodesoftware.protocol.MessageBuffer;
import com.icodesoftware.protocol.Messenger;
import com.icodesoftware.util.CipherUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.crypto.Cipher;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashSet;


public class
MessageBufferTest {

    @BeforeClass
    public void setUp() {
    }

    @Test(groups = { "message buffer" })
    public void ackMessageBufferTest() {
        HashSet<MessageBuffer.MessageHeader> headers = new HashSet<>();
        MessageBuffer messageBuffer = new MessageBuffer();
        messageBuffer.writeHeader(new MessageBuffer.MessageHeader());
        for(int i=0;i<20;i++){
            MessageBuffer.MessageHeader header = new MessageBuffer.MessageHeader();
            header.sequence = (i+1)*10000;
            Assert.assertTrue(headers.add(header));
            messageBuffer.writeHeader(header);
        }
        messageBuffer.flip();
        messageBuffer.readHeader();
        for(int i=0;i<20;i++){
            MessageBuffer.MessageHeader h = messageBuffer.readHeader();
            Assert.assertEquals(h.sequence,(i+1)*10000);
            Assert.assertTrue(headers.remove(h));
        }
        Assert.assertTrue(headers.isEmpty());
    }
    @Test(groups = { "message buffer" })
    public void resetMessageBufferTest(){
        MessageBuffer messageBuffer = new MessageBuffer();
        messageBuffer.writeHeader(new MessageBuffer.MessageHeader());
        messageBuffer.flip();
        byte[] buffer = new byte[MessageBuffer.SIZE];
        Assert.assertEquals(messageBuffer.toArray(buffer),MessageBuffer.HEADER_SIZE);
        buffer[0]='1';
        buffer[0]='2';
        buffer[0]='3';
        messageBuffer.reset(buffer,0,3);
        messageBuffer.writeHeader(new MessageBuffer.MessageHeader());
        messageBuffer.flip();
        Assert.assertEquals(messageBuffer.toArray(buffer),MessageBuffer.HEADER_SIZE+3);
        messageBuffer.reset();
        messageBuffer.writeHeader(new MessageBuffer.MessageHeader());
        messageBuffer.flip();
        Assert.assertEquals(messageBuffer.toArray(buffer),MessageBuffer.HEADER_SIZE);

    }
    @Test(groups = { "message buffer" })
    public void readMessageBufferTest(){
        MessageBuffer messageBuffer = new MessageBuffer();
        messageBuffer.writeHeader(new MessageBuffer.MessageHeader());
        messageBuffer.writeHeader(new MessageBuffer.MessageHeader());
        messageBuffer.flip();
        messageBuffer.readHeader();
        messageBuffer.rewind();
        byte[] buffer = new byte[MessageBuffer.SIZE];
        Assert.assertEquals(messageBuffer.toArray(buffer),MessageBuffer.HEADER_SIZE*2);
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
    @Test(groups = { "message buffer" })
    public void bufferMessageHeaderTest(){
        MessageBuffer.MessageHeader messageHeader = new MessageBuffer.MessageHeader();
        messageHeader.encrypted = false;
        messageHeader.ack = false;
        messageHeader.broadcasting = false;
        MessageBuffer messageBuffer = new MessageBuffer();
        messageBuffer.writeHeader(messageHeader);
        messageBuffer.writeUTF8("test");
        messageBuffer.writeInt(101);
        messageBuffer.flip();
        byte[] buffer = new byte[MessageBuffer.SIZE];
        int len = messageBuffer.toArray(buffer);
        MessageBuffer m = new MessageBuffer();
        m.reset(buffer,0,len);
        m.flip();
        messageBuffer.flip();
        MessageBuffer.MessageHeader _h = messageBuffer.readHeader();
        MessageBuffer.MessageHeader h1 = m.readHeader();
        Assert.assertEquals(h1.ack,_h.ack);
        Assert.assertEquals(h1.encrypted,_h.encrypted);
        Assert.assertEquals(h1.broadcasting,_h.broadcasting);
        String t = m.readUTF8();
        int f = m.readInt();
        String r = messageBuffer.readUTF8();
        int g = messageBuffer.readInt();
        Assert.assertEquals("test",r);
        Assert.assertEquals("test",t);
        Assert.assertEquals(101,f);
        Assert.assertEquals(101,g);
    }
    @Test(groups = { "message buffer" })
    public void bufferPayloadMessageTest1(){
        MessageBuffer.MessageHeader messageHeader = new MessageBuffer.MessageHeader();
        messageHeader.encrypted = false;
        messageHeader.ack = false;
        messageHeader.broadcasting = false;
        MessageBuffer messageBuffer = new MessageBuffer();
        messageBuffer.writeHeader(messageHeader);
        byte[] buffer1 = new byte[MessageBuffer.SIZE];
        for(int i=0;i<100;i++){
            buffer1[i]='a';
        }
        messageBuffer.writePayload(buffer1,0,100);
        messageBuffer.flip();
        messageBuffer.readHeader();
        byte[] buffer2 = messageBuffer.readPayload();
        Assert.assertEquals(buffer2.length,100);
        byte[] buffer3 = Arrays.copyOf(buffer1,100);
        Assert.assertEquals(buffer2,buffer3);
    }

    @Test(groups = { "message buffer" })
    public void bufferPayloadMessageTest2(){
        MessageBuffer.MessageHeader messageHeader = new MessageBuffer.MessageHeader();
        messageHeader.encrypted = false;
        messageHeader.ack = false;
        messageHeader.broadcasting = false;
        MessageBuffer messageBuffer = new MessageBuffer();
        messageBuffer.writeHeader(messageHeader);
        byte[] buffer1 = new byte[MessageBuffer.SIZE];
        for(int i=0;i<100;i++){
            buffer1[i]='a';
        }
        messageBuffer.writePayload(buffer1,0,100);
        messageBuffer.flip();
        messageBuffer.readHeader();
        byte[] buffer2 = new byte[MessageBuffer.SIZE];
        int length = messageBuffer.readPayload(buffer2);
        Assert.assertEquals(length,100);
        Assert.assertEquals(Arrays.copyOf(buffer1,100),Arrays.copyOf(buffer2,100));
    }

    @Test(groups = { "message buffer" })
    public void bufferPayloadMessageTest3(){
        int seq = 1;
        MessageBuffer.MessageHeader messageHeader = new MessageBuffer.MessageHeader();
        messageHeader.sequence = seq++;
        messageHeader.encrypted = false;
        messageHeader.ack = false;
        messageHeader.broadcasting = false;
        messageHeader.batchSize = 10;
        messageHeader.commandId = Messenger.REQUEST;
        MessageBuffer messageBuffer = new MessageBuffer();
        messageBuffer.writeHeader(messageHeader);
        messageBuffer.writeInt(2).writeUTF8("kills").writeFloat(1);//4*3 +5 = 17
        for(int i=0;i<10;i++){
            messageBuffer.writeInt(seq++).writeUTF8("kills").writeFloat(1);
        }
        messageBuffer.flip();
        messageBuffer.readHeader();
        Assert.assertEquals(messageBuffer.readInt(),2);
        Assert.assertEquals(messageBuffer.readUTF8(),"kills");
        Assert.assertEquals(messageBuffer.readFloat(),1.0f);

    }

}
