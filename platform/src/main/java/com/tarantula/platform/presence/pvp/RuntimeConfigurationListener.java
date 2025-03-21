package com.tarantula.platform.presence.pvp;

import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.configuration.FileCredentialConfiguration;


public class RuntimeConfigurationListener implements Configurable.Listener<FileCredentialConfiguration> {

    private static final TarantulaLogger logger = JDKLogger.getLogger(RuntimeConfigurationListener.class);
    public static final String CONFIG_NAME = "pvp";

    public void onLoaded(FileCredentialConfiguration loaded){
        logger.warn("Loaded");
        JsonObject config = JsonUtil.parse(loaded.load());
        logger.warn(config.toString());
    }

    public void onRemoved(FileCredentialConfiguration removed){
       logger.warn("Removed");
    }
}
