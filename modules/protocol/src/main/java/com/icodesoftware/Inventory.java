package com.icodesoftware;

import java.util.List;

public interface Inventory extends Configurable,Balance,Countable{

    String DataStore = "inventory";
    boolean rechargeable();

    List<Stock> onStock();

    interface Stock extends Configurable{
        long stockId();
    }

    interface Listener{
        void onInventory(Inventory inventory, Stock inventoryItem);
    }
}
