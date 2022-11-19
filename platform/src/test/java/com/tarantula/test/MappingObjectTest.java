package com.tarantula.test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.icodesoftware.util.CipherUtil;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.MappingObject;
import com.tarantula.platform.service.PresenceKey;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;

public class MappingObjectTest {

    @BeforeClass
    public void setUp() {
    }

    @Test(groups = { "MappingObejct" })
    public void mappingObjectTest() {
        byte[] jsonArray = "[1,2,3]".getBytes();
        MappingObject mappingObject = new MappingObject();
        mappingObject.value(jsonArray);

        MappingObject copy = new MappingObject();
        copy.fromBinary(mappingObject.toBinary());

        Assert.assertEquals(true,Arrays.equals(mappingObject.value(),copy.value()));
    }
    @Test(groups = { "MappingObejct" })
    public void presenceKeyTest() {
        String bkey = CipherUtil.toBase64Key();
        byte[] key = CipherUtil.fromBase64Key(bkey);
        PresenceKey mappingObject = new PresenceKey();
        mappingObject.base64key(bkey);
        JsonObject json = JsonUtil.toJsonObject(mappingObject.toMap());
        PresenceKey presenceKey = new PresenceKey();
        presenceKey.fromBinary(json.toString().getBytes());
        byte[] akey = presenceKey.toKey();//Base64.getDecoder().decode(json.get("_key").getAsString());
        Assert.assertEquals(true,Arrays.equals(key,akey));
    }
}
