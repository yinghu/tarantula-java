package com.icodesoftware.test;


import com.icodesoftware.util.CompressUtil;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;
import net.jpountz.lz4.LZ4SafeDecompressor;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;


public class CompressUtilTest {

    @BeforeClass
    public void setUp() {
    }

    @Test(groups = { "compress test" })
    public void compressTest() {
        ByteBuffer buffer = ByteBuffer.allocateDirect(21);
        buffer.putLong(100).putLong(200).put("hello".getBytes());
        buffer.flip();
        ByteBuffer dest = ByteBuffer.allocateDirect(21);
        CompressUtil.compress(buffer,dest);
        dest.flip();
        buffer.clear();
        CompressUtil.decompress(dest,buffer);
        buffer.flip();
        Assert.assertEquals(buffer.getLong(),100);
        Assert.assertEquals(buffer.getLong(),200);
        byte[] hello = new byte[5];
        buffer.get(hello);
        Assert.assertEquals(new String(hello),"hello");
    }

    @Test(groups = { "compress test" })
    public void lz4test(){
        CompressUtil.LZ4 lz4 = CompressUtil.lz4();
        ByteBuffer buffer = ByteBuffer.allocateDirect(21);
        buffer.putLong(100).putLong(200).put("hello".getBytes());
        buffer.flip();
        ByteBuffer dest = ByteBuffer.allocateDirect(21);
        lz4.compress(buffer,dest);
        dest.flip();
        buffer.clear();
        lz4.decompress(dest,buffer);
        buffer.flip();
        Assert.assertEquals(buffer.getLong(),100);
        Assert.assertEquals(buffer.getLong(),200);
        byte[] hello = new byte[5];
        buffer.get(hello);
        Assert.assertEquals(new String(hello),"hello");
    }
}
