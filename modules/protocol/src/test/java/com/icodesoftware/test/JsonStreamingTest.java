package com.icodesoftware.test;

import com.icodesoftware.JsonStreamingHandler;
import com.icodesoftware.util.JsonStreaming;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class JsonStreamingTest implements JsonStreamingHandler {

    @BeforeClass
    public void setUp() {
    }
    @Test(groups = { "json streaming" })
    public void testOne(){
        Assert.assertTrue(JsonStreaming.handle(Thread.currentThread().getContextClassLoader().getResourceAsStream("simple.json"),this));
        //Assert.assertTrue(JsonStreaming.handle(Thread.currentThread().getContextClassLoader().getResourceAsStream("sample-shop.json"),this));
    }

    @Override
    public void onBeginObject(String tag,int index) {
        System.out.println("Start Object : "+tag+ " : "+index);
    }

    @Override
    public boolean onEndObject(String tag,int index) {
        System.out.println("END : "+tag+" : "+index);
        return false;
    }

    @Override
    public void onBeginArray(String tag,int index) {
        System.out.println("START Array : "+tag+" : "+index);
    }

    @Override
    public boolean onEndArray(String tag,int index) {
        System.out.println("END Array : "+tag+" : "+index);
        return true;
    }

    @Override
    public boolean onString(String tag,String value,int index){
        System.out.println("VALUE : "+tag+" : "+value+" : "+index);
        return false;
    }
    @Override
    public boolean onNumber(String tag,Number value,int index){
        System.out.println("VALUE : "+tag+" : "+value.intValue()+" ; "+index);
        return false;
    }

    @Override
    public boolean onBoolean(String tag,boolean value,int index){
        System.out.println("VALUE : "+tag+" : "+value+" ; "+index);
        return false;
    }

    @Override
    public boolean onNull(String tag,int index) {
        System.out.println("NULL VALUE : "+tag+" : "+index);
        return false;
    }
}
