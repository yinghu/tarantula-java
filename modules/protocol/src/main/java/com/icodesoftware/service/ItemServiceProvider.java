package com.icodesoftware.service;

import com.icodesoftware.Consumable;

public interface ItemServiceProvider extends ServiceProvider{

    String registerListener(Consumable.Listener listener);

    Consumable register(Consumable item);
}
