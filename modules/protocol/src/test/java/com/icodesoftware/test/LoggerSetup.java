package com.icodesoftware.test;

import org.testng.annotations.BeforeClass;

public class LoggerSetup {

    static {
        System.setProperty("java.util.logging.manager","com.icodesoftware.logging.TarantulaLogManager");
    }
    @BeforeClass
    public void setUp() {
    }
}
