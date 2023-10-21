package com.icodesoftware;

import com.icodesoftware.service.ApplicationPreSetup;

import java.util.List;

public interface Inventory extends Configurable,Balance,Countable{

    String DataStore = "inventory";
    boolean rechargeable();

    boolean constrained();
    String typeId();
    String type();
    List<Stock> onStock();

    int stockFactoryId();
    int stockClassId();
    void stockFactoryId(int stockFactoryId);
    void stockClassId(int stockClassId);

    interface Stock extends Configurable{

        long itemId();
        long stockId();
    }

    interface Listener{
        void onInventory(ApplicationPreSetup applicationPreSetup,Inventory inventory, Stock inventoryItem);
    }
}
