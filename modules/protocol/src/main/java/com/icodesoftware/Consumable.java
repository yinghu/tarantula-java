package com.icodesoftware;

import com.google.gson.JsonObject;

public interface Consumable extends Configuration {

    JsonObject toJson();
}
