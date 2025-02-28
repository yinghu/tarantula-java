package com.icodesoftware.protocol;

import com.icodesoftware.*;

public class TRTarantulaApplication implements TarantulaApplication {

    protected ApplicationContext applicationContext;

    public void initialize(Session session) throws Exception{}

    public void callback(Session session) throws Exception{}

    public void callback(Session session,byte[] payload) throws Exception{}


    public void clear(){}

    @Override
    public void onError(Session session, Exception ex) {

    }

    @Override
    public boolean onEvent(Event event) {
        return false;
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        this.applicationContext = context;
    }

    @Override
    public Descriptor descriptor() {
        return applicationContext.descriptor();
    }

}
