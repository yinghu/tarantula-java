package com.icodesoftware;

import com.google.gson.JsonObject;
import com.icodesoftware.service.ApplicationPreSetup;

import java.util.List;

public interface Inventory extends Configurable,Balance,Countable{

    String DataStore = "inventory";
    boolean rechargeable();

    boolean constrained();
    String typeId();
    String type();
    List<Stock> onStock();
    Stock stock(long stockId);

    int stockFactoryId();
    int stockClassId();
    void stockFactoryId(int stockFactoryId);
    void stockClassId(int stockClassId);

    void removeStock(Stock stock);

    interface Stock extends Configurable{

        long itemId();
        long stockId();
        JsonObject assembly();
    }

    interface Listener{
        void onInventory(ApplicationPreSetup applicationPreSetup,Inventory inventory, Stock inventoryItem);
    }
}
