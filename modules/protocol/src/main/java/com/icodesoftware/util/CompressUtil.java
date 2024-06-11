package com.icodesoftware.util;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;

import java.nio.ByteBuffer;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class CompressUtil {

    public static void compress(ByteBuffer src, ByteBuffer dest) {
        Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
        try {
            deflater.setInput(src);
            deflater.finish();
            deflater.deflate(dest, Deflater.SYNC_FLUSH);
            if (src.hasRemaining()) {
                throw new RuntimeException("dest buffer too small");
            }
        }
        catch (Exception ex){
            throw new RuntimeException(ex);
        }
        finally {
            deflater.end();
        }
    }

    public static void decompress(ByteBuffer src, ByteBuffer dst) {
        Inflater inflater = new Inflater(true);
        try {
            inflater.setInput(src);
            inflater.inflate(dst);
            if (src.hasRemaining()) {
                throw new RuntimeException("dest buffer too small");
            }
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
        finally {
            inflater.end();
        }
    }

    public static void lz4Compress(ByteBuffer src, ByteBuffer dest){
        LZ4Factory factory = LZ4Factory.fastestInstance();
        LZ4Compressor compressor = factory.fastCompressor();
        compressor.compress(src,dest);
        if (src.hasRemaining()) {
            throw new RuntimeException("dest buffer too small");
        }
    }

    public static void lz4Decompress(ByteBuffer src, ByteBuffer dest){
        LZ4Factory factory = LZ4Factory.fastestInstance();
        LZ4FastDecompressor decompressor = factory.fastDecompressor();
        decompressor.decompress(src,dest);
        if (src.hasRemaining()) {
            throw new RuntimeException("dest buffer too small");
        }
    }
}
