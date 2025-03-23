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
    public void onBeginObject(String tag) {
        System.out.println("Start Object : "+tag);
    }

    @Override
    public void onEndObject(String tag) {
        System.out.println("END : "+tag);
    }

    @Override
    public void onBeginArray(String tag) {
        System.out.println("START Array : "+tag);
    }

    @Override
    public void onEndArray(String tag) {
        System.out.println("END Array : "+tag);
    }

    @Override
    public void onString(String tag,String value){
        System.out.println("VALUE : "+tag+" : "+value);
    }
    @Override
    public void onNumber(String tag,Number value){
        System.out.println("VALUE : "+tag+" : "+value.intValue());
    }

    @Override
    public void onBoolean(String tag,boolean value){
        System.out.println("VALUE : "+tag+" : "+value);
    }

    @Override
    public void onNull(String tag) {
        System.out.println("NULL VALUE : "+tag+" : ");
    }
}
