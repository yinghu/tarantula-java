package com.icodesoftware.test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.util.JsonUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


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
}
