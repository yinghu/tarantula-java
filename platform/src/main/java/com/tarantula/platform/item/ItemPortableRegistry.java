package com.tarantula.platform.item;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.AbstractRecoverableListener;
import com.tarantula.platform.configuration.ConfigurationObject;
import com.tarantula.platform.configuration.VendorConfiguration;
import com.tarantula.platform.inventory.Inventory;
import com.tarantula.platform.inventory.InventoryItem;
import com.tarantula.platform.store.Shop;
import com.tarantula.platform.store.ShoppingItem;
import com.tarantula.platform.store.Transaction;


public class ItemPortableRegistry<T extends Recoverable> extends AbstractRecoverableListener {

    public static final int OID = 7;

    public static final int CONFIGURABLE_OBJECT_CID = 1;

    public static final int ASSET_CID = 2;
    public static final int COMPONENT_CID = 3;
    public static final int COMMODITY_CID = 4;
    public static final int ITEM_CID = 5;
    public static final int APPLICATION_CID = 6;

    public static final int INVENTORY_CID = 7;

    public static final int INVENTORY_ITEM_CID = 8;

    public static final int CATEGORY_CID = 9;
    public static final int CATEGORY_ITEM_CID = 10;

    public static final int REFERENCE_INDEX_CID = 11;

    public static final int SHOPPING_ITEM_CID = 13;

    public static final int TRANSACTION_CID = 14;

    public static final int CONFIGURABLE_TEMPLATE = 15;

    public static final int CONFIGURABLE_TYPES_CID = 16;

    public static final int CONFIGURABLE_CATEGORIES_CID = 17;

    public static final int SHOP_CID = 18;

    public static final int CONFIGURATION_OBJECT_CID = 19;

    public static final int VENDOR_CONFIGURATION_CID = 20;

    public static final int CONFIGURABLE_CATEGORY_CID = 21;

    public static final int CONFIGURABLE_TYPE_CID = 22;

    public static final int VERSIONED_CONFIGURABLE_OBJECT_CID = 23;


    public T create(int i) {
        Recoverable pt = null;
        switch (i){
            case CONFIGURABLE_OBJECT_CID:
                pt = new ConfigurableObject();
                break;
            case ASSET_CID:
                pt = new Asset();
                break;
            case COMPONENT_CID:
                pt = new Component();
                break;
            case COMMODITY_CID:
                pt = new Commodity();
                break;
            case ITEM_CID:
                pt = new Item();
                break;
            case APPLICATION_CID:
                pt = new Application();
                break;
            case INVENTORY_CID:
                pt = new Inventory();
                break;
            case INVENTORY_ITEM_CID:
                pt = new InventoryItem();
                break;
            //case CATEGORY_CID:
                //pt = new Category();
                //break;
            case CATEGORY_ITEM_CID:
                pt = new CategoryItem();
                break;
            case REFERENCE_INDEX_CID:
                pt = new ReferenceIndex();
                break;
            case SHOPPING_ITEM_CID:
                pt = new ShoppingItem();
                break;
            case TRANSACTION_CID:
                pt = new Transaction();
                break;
            case CONFIGURABLE_TEMPLATE:
                pt = new ConfigurableTemplate();
                break;
            case CONFIGURABLE_TYPES_CID:
                pt = new ConfigurableTypes();
                break;
            case CONFIGURABLE_CATEGORIES_CID:
                pt = new ConfigurableCategories();
                break;
            case SHOP_CID:
                pt = new Shop();
                break;
            case CONFIGURATION_OBJECT_CID:
                pt = new ConfigurationObject();
                break;
            case VENDOR_CONFIGURATION_CID:
                pt = new VendorConfiguration();
                break;
            case CONFIGURABLE_CATEGORY_CID:
                pt = new ConfigurableCategory();
                break;
            case CONFIGURABLE_TYPE_CID:
                pt = new ConfigurableType();
                break;
            case VERSIONED_CONFIGURABLE_OBJECT_CID:
                pt = new VersionedConfigurableObject();
                break;
            default:
        }
        return (T)pt;
    }

    public int registryId() {
        return OID;
    }
}
