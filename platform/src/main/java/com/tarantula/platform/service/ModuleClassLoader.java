package com.tarantula.platform.service;

import java.lang.instrument.Instrumentation;

abstract public class ModuleClassLoader extends ClassLoader {

    abstract public void reset(Instrumentation instrumentation);
}
