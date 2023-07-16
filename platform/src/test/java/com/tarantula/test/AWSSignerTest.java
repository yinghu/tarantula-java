package com.tarantula.test;

import com.tarantula.platform.configuration.AWSSigner;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class AWSSignerTest {

    @BeforeClass
    public void setUp() {

    }

    @Test(groups = { "Signing" })
    public void signTest() {
        AWSSigner signer = new AWSSigner();
        Exception exception = null;
        try {
            String date = AWSSigner.signingDate();
            signer.init("klvu5syA4q4I/6cf+kfsaIKVnVOS9bQpFLERX9op");
            signer.sign("GET",date,"/");
        }catch (Exception  ex){
            exception = ex;
        }
        Assert.assertNull(exception);
    }
}
