package com.tarantula.test;

import com.google.gson.JsonObject;
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
}
