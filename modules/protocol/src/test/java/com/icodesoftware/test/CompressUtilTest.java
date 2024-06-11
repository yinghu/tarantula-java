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
        ByteBuffer buffer = ByteBuffer.allocateDirect(21);
        buffer.putLong(100).putLong(200).put("hello".getBytes());
        buffer.flip();
        ByteBuffer dest = ByteBuffer.allocateDirect(21);
        CompressUtil.lz4Compress(buffer,dest);
        dest.flip();
        buffer.clear();
        CompressUtil.lz4Decompress(dest,buffer);
        buffer.flip();
        Assert.assertEquals(buffer.getLong(),100);
        Assert.assertEquals(buffer.getLong(),200);
        byte[] hello = new byte[5];
        buffer.get(hello);
        Assert.assertEquals(new String(hello),"hello");
    }
}
