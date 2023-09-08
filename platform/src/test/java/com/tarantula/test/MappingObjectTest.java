package com.tarantula.test;


import com.tarantula.game.MappingObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

public class MappingObjectTest {



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
