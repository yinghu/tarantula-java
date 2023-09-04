package com.tarantula.platform.item;


import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;

public class CategoryItemQuery implements RecoverableFactory<CategoryItem> {


    private Recoverable.Key key;
    private String label;
    public CategoryItemQuery(Recoverable.Key key, String label) {
        this.key = key;
        this.label = label;
    }
    @Override
    public CategoryItem create() {
        return new CategoryItem();
    }

    @Override
    public int registryId() {
        return 0;
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public Recoverable.Key key() {
        return key;
    }

}
