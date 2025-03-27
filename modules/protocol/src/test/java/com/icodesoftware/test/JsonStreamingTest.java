package com.icodesoftware.test;

import com.icodesoftware.JsonStreamingHandler;
import com.icodesoftware.protocol.configuration.TRProperty;
import com.icodesoftware.protocol.configuration.TRTemplate;
import com.icodesoftware.service.Template;
import com.icodesoftware.util.JsonStreaming;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class JsonStreamingTest implements JsonStreamingHandler {

    TRTemplate trTemplate = new TRTemplate();
    @BeforeClass
    public void setUp() {
    }
    @Test(groups = { "json streaming" })
    public void testOne(){
        Assert.assertTrue(JsonStreaming.handle(Thread.currentThread().getContextClassLoader().getResourceAsStream("simple.json"),this));
        Assert.assertNotNull(trTemplate.scope());
        Assert.assertNotNull(trTemplate.type());
        Assert.assertNotNull(trTemplate.version());
        Assert.assertTrue(trTemplate.rechargeable());
        Assert.assertEquals(trTemplate.properties().size(),3);
        for(Template.Property p : trTemplate.properties()){
            Assert.assertNotNull(p.name());
            Assert.assertNotNull(p.type());
            Assert.assertNotNull(p.reference());
            Assert.assertTrue(!p.downloadable());
        }
    }

    @Override
    public void onBeginObject(String tag,int index) {
        //System.out.println("Start Object : "+tag+ " : "+index);
        if(tag.equals("$.application.properties["+index+"]")){
            trTemplate.property(new TRProperty());
        }
    }

    @Override
    public boolean onEndObject(String tag,int index) {
        //System.out.println("END : "+tag+" : "+index);
        return false;
    }

    @Override
    public void onBeginArray(String tag,int index) {
        //System.out.println("START Array : "+tag+" : "+index);
        if(tag.equals(" $.application.properties")){

        }
    }

    @Override
    public boolean onEndArray(String tag,int index) {
        //System.out.println("END Array : "+tag+" : "+index);
        return false;
    }

    @Override
    public boolean onString(String tag,String value,int index){
        //System.out.println("VALUE : "+tag+" : "+value+" : "+index);
        if(tag.equals("$.header.scope")){
            trTemplate.scope(value);
        }
        if(tag.equals("$.header.type")){
            trTemplate.type(value);
        }
        if(tag.equals("$.header.version")){
            trTemplate.version(value);
        }
        if(tag.equals("$.application.properties["+index+"].name")){
            ((TRProperty)trTemplate.properties().get(index)).name(value);
        }
        if(tag.equals("$.application.properties["+index+"].type")){
            ((TRProperty)trTemplate.properties().get(index)).type(value);
        }
        if(tag.equals("$.application.properties["+index+"].reference")){
            ((TRProperty)trTemplate.properties().get(index)).reference(value);
        }
        return false;
    }
    @Override
    public boolean onNumber(String tag,Number value,int index){
        //System.out.println("VALUE : "+tag+" : "+value.intValue()+" ; "+index);
        return false;
    }

    @Override
    public boolean onBoolean(String tag,boolean value,int index){
        //System.out.println("VALUE : "+tag+" : "+value+" ; "+index);
        if(tag.equals("$.header.rechargeable")){
            trTemplate.rechargeable(value);
        }
        if(tag.equals("$.application.properties["+index+"].downloadable")){
            ((TRProperty)trTemplate.properties().get(index)).downloadable(value);
        }
        return false;
    }

    @Override
    public boolean onNull(String tag,int index) {
        //System.out.println("NULL VALUE : "+tag+" : "+index);
        return false;
    }
}
