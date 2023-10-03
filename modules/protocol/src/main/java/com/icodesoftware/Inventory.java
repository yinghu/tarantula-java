package com.icodesoftware;

import java.util.List;

public interface Inventory extends Configurable,Balance,Countable{

    boolean rechargeable();

    List<Configurable> itemList();
}
