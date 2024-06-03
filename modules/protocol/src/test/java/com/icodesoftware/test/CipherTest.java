package com.icodesoftware.test;

import com.icodesoftware.protocol.MessageBuffer;
import com.icodesoftware.util.BatchUtil;
import com.icodesoftware.util.CipherUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.crypto.Cipher;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class CipherTest {

    @BeforeClass
    public void setUp() {
    }

    @Test(groups = { "cipher util" })
    public void sizeTest(){
        try {
            byte[] key = CipherUtil.key();
            Cipher cipher = CipherUtil.encrypt(key);
            byte[] plain = new byte[MessageBuffer.PAYLOAD_SIZE - CipherUtil.cipherSize(MessageBuffer.PAYLOAD_SIZE)];
            for(int i=0;i<plain.length;i++){
                plain[i]= 'c';
            }
            byte[] ret = cipher.doFinal(plain,0,plain.length);
            Assert.assertEquals(ret.length,plain.length+CipherUtil.cipherSize(plain.length));
            Assert.assertTrue(ret.length<MessageBuffer.PAYLOAD_SIZE);
        }catch (Exception exception){
            exception.printStackTrace();
        }
    }

    @Test(groups = { "cipher util" })
    public void batchTest(){
        try {
            byte[] key = CipherUtil.key();
            Cipher cipher = CipherUtil.encrypt(key);
            Cipher de = CipherUtil.decrypt(key);
            byte[] plain = new byte[MessageBuffer.PAYLOAD_SIZE*10+140];
            for(int i=0;i<plain.length;i++){
                plain[i]= 'c';
            }
            BatchUtil.Batch batch = BatchUtil.batch(plain.length,MessageBuffer.PAYLOAD_SIZE-CipherUtil.cipherSize(MessageBuffer.PAYLOAD_SIZE));
            StringBuffer sb = new StringBuffer();
            batch.offsets.forEach(b->{
                ByteBuffer byteBuffer = ByteBuffer.allocate(b.length);
                byteBuffer.put(plain,b.offset,b.length);
                byteBuffer.flip();
                try{
                    byte[] pp = byteBuffer.array();
                    byte[] ret = cipher.doFinal(byteBuffer.array());
                    Assert.assertEquals(pp.length,b.length);
                    Assert.assertTrue(ret.length < MessageBuffer.PAYLOAD_SIZE);
                    byte[] back = de.doFinal(ret);
                    for(byte c: back){
                        sb.append((char)c);
                    }
                    Assert.assertEquals(back.length,b.length);
                }
                catch (Exception ex){
                    ex.printStackTrace();
                }
            });
            Assert.assertEquals(sb.length(),plain.length);
        }catch (Exception exception){
            exception.printStackTrace();
        }
    }

    @Test(groups = { "cipher util" })
    public void keyBase64Test(){
        String bkey = CipherUtil.toBase64Key();
        byte[] key = CipherUtil.fromBase64Key(bkey);
        String akey = CipherUtil.toBase64Key(key);
        byte[] key1 = CipherUtil.fromBase64Key(akey);
        Assert.assertEquals(akey,bkey);
        Assert.assertTrue(Arrays.equals(key1,key));
    }


}
