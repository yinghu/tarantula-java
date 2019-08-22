package com.tarantula.demo;

import com.tarantula.ApplicationContext;
import com.tarantula.Module;
import com.tarantula.OnLog;
import com.tarantula.Session;

public class DemoService implements Module {

    private ApplicationContext context;

    @Override
    public boolean onRequest(Session session, byte[] bytes, OnUpdate onUpdate) throws Exception {
        session.write(bytes,this.label());
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        this.context.registerRecoverableListener(new DemoPortableRegistry()).addRecoverableFilter(DemoPortableRegistry.TIMER_OID,(t)->{
            this.context.log(t.toString(),OnLog.INFO);
        });
        this.context.log("DemoService started", OnLog.INFO);
    }

    @Override
    public String label() {
        return "demo";
    }

    @Override
    public void clear(){

    }
}
