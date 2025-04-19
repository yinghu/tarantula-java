package com.icodesoftware.util;

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

}
