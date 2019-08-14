package com.tarantula.logging;

import java.util.logging.LogManager;

//JDK LogManager shutdown hook
public class TarantulaLogManager extends LogManager {


    static TarantulaLogManager instance;

    public TarantulaLogManager(){
        instance = this;
    }

    @Override
    public void reset() {

    }
    private void _reset() { super.reset(); }

    public static void shutdown(){
        instance._reset();
    }
}
