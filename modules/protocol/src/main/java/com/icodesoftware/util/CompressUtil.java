package com.icodesoftware.util;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4SafeDecompressor;

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


    public static LZ4 lz4(){
        return new LZ4();
    }

    public static class LZ4{

        private static LZ4Factory factory;

        static {
            factory = LZ4Factory.fastestJavaInstance();
        }

        private LZ4(){}

        public void compress(ByteBuffer src, ByteBuffer dest){
            LZ4Compressor compressor = factory.fastCompressor();
            compressor.compress(src,dest);
            if (src.hasRemaining()) {
                throw new RuntimeException("dest buffer too small");
            }
        }

        public void decompress(ByteBuffer src, ByteBuffer dest){
            LZ4SafeDecompressor decompressor = factory.safeDecompressor();
            decompressor.decompress(src,dest);
            if (src.hasRemaining()) {
                throw new RuntimeException("dest buffer too small");
            }
        }
    }
}
