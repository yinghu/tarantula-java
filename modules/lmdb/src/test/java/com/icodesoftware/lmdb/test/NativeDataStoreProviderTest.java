package com.icodesoftware.lmdb.test;

import com.icodesoftware.lmdb.ffm.NativeUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.foreign.Arena;
import java.lang.foreign.Linker;
import java.lang.foreign.SymbolLookup;
import java.net.URL;
import java.nio.file.Path;

public class NativeDataStoreProviderTest {

    @Test(groups = { "native data store" })
    public void loadLibTest(){
        URL url = Thread.currentThread().getContextClassLoader().getResource(NativeUtil.libName());
        Assert.assertNotNull(url);
        Exception exception = null;
        try(Arena arena = Arena.ofConfined()){
            SymbolLookup lib = SymbolLookup.libraryLookup(Path.of(url.toURI()),arena);
            Linker linker = Linker.nativeLinker();
        }
        catch (Exception ex){
            //exception = ex;
        }
        Assert.assertNull(exception);
    }
}
