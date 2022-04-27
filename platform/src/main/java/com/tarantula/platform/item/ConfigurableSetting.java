package com.tarantula.platform.item;

import com.google.gson.JsonArray;

public class ConfigurableSetting {

    public String scope;
    public String type;//category
    public String description;
    public boolean rechargeable;
    public String version;

    public JsonArray properties;

}
