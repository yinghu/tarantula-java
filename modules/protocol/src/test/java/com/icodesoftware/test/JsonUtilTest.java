package com.icodesoftware.test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.icodesoftware.Recoverable;
import com.icodesoftware.util.*;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class JsonUtilTest {

    @BeforeClass
    public void setUp() {
    }

    @Test(groups = { "json util" })
    public void simpleKeyValueTest() {
        Map<String,Object> props = new HashMap<>();
        props.put("name","aname");
        props.put("age",1);
        props.put("valid",true);
        JsonObject json = JsonUtil.toJsonObject(props);
        Assert.assertEquals(json.get("name").getAsString(),"aname");
        Assert.assertEquals(json.get("age").getAsInt(),1);
        Assert.assertEquals(json.get("valid").getAsBoolean(),true);
    }
    @Test(groups = { "json util" })
    public void jsonObjectTest() {
        Map<String,Object> props = new HashMap<>();
        props.put("name","aname");
        props.put("age",1);
        props.put("valid",true);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("p",1);
        props.put("js",jsonObject);
        JsonObject json = JsonUtil.toJsonObject(props);
        Assert.assertEquals(json.get("name").getAsString(),"aname");
        Assert.assertEquals(json.get("age").getAsInt(),1);
        Assert.assertEquals(json.get("valid").getAsBoolean(),true);
        Assert.assertEquals(json.get("js").getAsJsonObject().get("p").getAsInt(),1);
    }
    @Test(groups = { "json util" })
    public void jsonArrayTest() {
        Map<String,Object> props = new HashMap<>();
        props.put("name","aname");
        props.put("age",1);
        props.put("valid",true);
        JsonArray jsonObject = new JsonArray();
        jsonObject.add(1);
        jsonObject.add(2);
        props.put("js",jsonObject);
        JsonObject json = JsonUtil.toJsonObject(props);
        Assert.assertEquals(json.get("name").getAsString(),"aname");
        Assert.assertEquals(json.get("age").getAsInt(),1);
        Assert.assertEquals(json.get("valid").getAsBoolean(),true);
        Assert.assertEquals(json.get("js").getAsJsonArray().get(1).getAsInt(),2);
    }

    @Test(groups = { "json util" })
    public void simpleJsonMapTest() {
        JsonObject props = new JsonObject();
        props.addProperty("name","aname");
        props.addProperty("age",1);
        props.addProperty("foo",12.01);
        props.addProperty("valid",true);
        Map<String,Object> map = JsonUtil.toMap(props.toString().getBytes());
        Assert.assertEquals((String) map.get("name"),"aname");
        Assert.assertEquals(((Number)map.get("age")).intValue(),1);
        Assert.assertEquals(((Number)map.get("foo")).floatValue(),12.01f);
        Assert.assertEquals((boolean)map.get("valid"),true);
    }
    @Test(groups = { "json util" })
    public void simpleJsonObjectMapTest() {
        JsonObject props = new JsonObject();
        props.addProperty("name","aname");
        props.addProperty("age",1);
        props.addProperty("foo",12.01);
        props.addProperty("valid",true);
        props.add("jo",new JsonObject());
        Map<String,Object> map = JsonUtil.toMap(props.toString().getBytes());
        Assert.assertEquals((String) map.get("name"),"aname");
        Assert.assertEquals(((Number)map.get("age")).intValue(),1);
        Assert.assertEquals(((Number)map.get("foo")).floatValue(),12.01f);
        Assert.assertEquals((boolean)map.get("valid"),true);
        Assert.assertEquals(((JsonElement)map.get("jo")).getAsJsonObject().toString(),"{}");
    }
    @Test(groups = { "json util" })
    public void simpleJsonArrayMapTest() {
        JsonObject props = new JsonObject();
        props.addProperty("name","aname");
        props.addProperty("age",1);
        props.addProperty("foo",12.01);
        props.addProperty("valid",true);
        props.add("jo",new JsonObject());
        props.add("ja",new JsonArray());

        Map<String,Object> map = JsonUtil.toMap(props.toString().getBytes());
        Assert.assertEquals((String) map.get("name"),"aname");
        Assert.assertEquals(((Number)map.get("age")).intValue(),1);
        Assert.assertEquals(((Number)map.get("foo")).floatValue(),12.01f);
        Assert.assertEquals((boolean)map.get("valid"),true);
        Assert.assertEquals(((JsonElement)map.get("jo")).getAsJsonObject().toString(),"{}");
        Assert.assertEquals(((JsonElement)map.get("ja")).getAsJsonArray().toString(),"[]");
    }

    @Test(groups = { "json util" })
    public void inputStreamJsonObjectTest() {
        JsonObject props = new JsonObject();
        props.addProperty("name","aname");
        props.addProperty("age",1);
        props.addProperty("foo",12);
        props.addProperty("valid",true);
        Recoverable.DataBuffer dataBuffer = BufferProxy.buffer(100,true);
        for(byte b : props.toString().getBytes()){
            dataBuffer.writeByte(b);
        }
        dataBuffer.flip();
        JsonObject cp = JsonUtil.parse(new DataBufferInputStream(dataBuffer));
        Assert.assertEquals(cp.get("name").getAsString(),"aname");
        Assert.assertEquals(cp.get("age").getAsInt(),1);
        Assert.assertEquals(cp.get("foo").getAsLong(),12);
        Assert.assertEquals(cp.get("valid").getAsBoolean(),true);
        try {
            DataBufferOutputStream out = new DataBufferOutputStream(100,true);
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(out));
            writer.beginObject().name("id").value(100).name("name").value("moon").endObject();
            writer.flush();
            InputStreamReader in = new InputStreamReader(new DataBufferInputStream(out.src()));
            JsonReader reader = new JsonReader(in);
            reader.beginObject();
            while (reader.hasNext()){
                String name = reader.nextName();
                if(name.equals("id")){
                    Assert.assertEquals(reader.nextInt(),100);
                    continue;
                }
                if(name.equals("name")){
                    Assert.assertEquals(reader.nextString(),"moon");
                }
            }
            reader.endObject();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Test(groups = { "json util" })
    public void bufferWriterTest(){
        DataBufferJsonWriter dataBufferWriter = new DataBufferJsonWriter(10,true);
        dataBufferWriter.start();
        dataBufferWriter.beginObject();
        dataBufferWriter.namedString("name","aname");
        dataBufferWriter.namedNumber("age",1);
        dataBufferWriter.endObject();
        dataBufferWriter.end();
        JsonObject src = JsonUtil.parse(dataBufferWriter.src().array());
        Assert.assertEquals(src.get("name").getAsString(),"aname");
        Assert.assertEquals(src.get("age").getAsInt(),1);

        dataBufferWriter.start();
        dataBufferWriter.beginObject();
        dataBufferWriter.namedString("name","bname");
        dataBufferWriter.endObject();
        dataBufferWriter.end();
        JsonObject src1 = JsonUtil.parse(dataBufferWriter.src().array());
        Assert.assertEquals(src1.get("name").getAsString(),"bname");

        dataBufferWriter.start().beginObject();
        dataBufferWriter.namedString("name","bname");
        dataBufferWriter.namedArray("classes").stringOfArray("a").stringOfArray("b").endArray();
        dataBufferWriter.endObject().end();
        JsonObject src3 = JsonUtil.parse(dataBufferWriter.src().array());
        Assert.assertEquals(src3.get("name").getAsString(),"bname");
        Assert.assertEquals(src3.get("classes").getAsJsonArray().size(),2);

        dataBufferWriter.start().beginObject().namedArray("list");
        for(int i=0;i<2;i++){
            dataBufferWriter.beginObject();
            dataBufferWriter.namedString("a","a").namedBoolean("b",true);
            dataBufferWriter.endObject();
        }
        dataBufferWriter.endArray().endObject().end();
        JsonObject src4 = (JsonUtil.parse(dataBufferWriter.src().array()));
        Assert.assertEquals(src4.get("list").getAsJsonArray().size(),2);

    }

}
