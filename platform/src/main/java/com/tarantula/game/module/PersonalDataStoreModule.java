package com.tarantula.game.module;

import com.icodesoftware.ApplicationContext;
import com.icodesoftware.Configurable;
import com.icodesoftware.OnLog;
import com.icodesoftware.Session;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.item.ConfigurableObject;


public class PersonalDataStoreModule extends ModuleHeader implements Configurable.Listener<ConfigurableObject> {

    @Override
    public boolean onRequest(Session session, byte[] bytes) throws Exception {
        session.write(JsonUtil.toSimpleResponse(true,"item call").getBytes());
        return false;
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        super.setup(context);
        gameServiceProvider.configurationServiceProvider().registerConfigurableListener(context.descriptor(),this);
        this.context.log("Game personal data module started", OnLog.WARN);
    }

}
