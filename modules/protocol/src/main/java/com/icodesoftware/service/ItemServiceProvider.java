package com.icodesoftware.service;

import com.icodesoftware.Consumable;

public interface ItemServiceProvider {

    void registerListener(Consumable.Listener listener);

    Consumable register(Consumable item);
}
