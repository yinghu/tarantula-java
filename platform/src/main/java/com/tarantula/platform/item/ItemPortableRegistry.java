package com.tarantula.platform.item;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.AbstractRecoverableListener;
import com.tarantula.platform.inventory.Inventory;
import com.tarantula.platform.inventory.InventoryItem;


public class ItemPortableRegistry extends AbstractRecoverableListener {

    public static final int OID = 7;

    public static final int CONFIGURABLE_OBJECT_CID = 1;
    public static final int ASSET_CID = 2;
    public static final int COMMODITY_CID = 3;
    public static final int ITEM_CID = 4;

    public static final int INVENTORY_CID = 5;

    public static final int INVENTORY_ITEM_CID = 6;

    public Recoverable create(int i) {
        Recoverable pt = null;
        switch (i){
            case CONFIGURABLE_OBJECT_CID:
                pt = new ConfigurableObject();
                break;
            case ASSET_CID:
                pt = new Asset();
                break;
            case COMMODITY_CID:
                pt = new Commodity();
                break;
            case ITEM_CID:
                pt = new Item();
                break;
            case INVENTORY_CID:
                pt = new Inventory();
                break;
            case INVENTORY_ITEM_CID:
                pt = new InventoryItem();
                break;
            default:
        }
        return pt;
    }

    public int registryId() {
        return OID;
    }
}
