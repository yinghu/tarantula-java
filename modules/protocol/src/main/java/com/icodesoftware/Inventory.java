package com.icodesoftware;

import java.util.List;

public interface Inventory extends Configurable,Balance,Countable{

    boolean rechargeable();

    List<Stock> onStock();

    interface Stock extends Configurable{
        long stockId();
    }
}
