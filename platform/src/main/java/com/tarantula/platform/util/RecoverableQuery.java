package com.tarantula.platform.util;


import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.RecoverableRegistry;


public class RecoverableQuery<T extends Recoverable> implements RecoverableFactory<T> {

    private Recoverable.Key key;
    private String label;

    private int classId;
    private RecoverableRegistry<T> registry;
    public RecoverableQuery(Recoverable.Key key, String label,int classId, RecoverableRegistry<T> registry){
        this.key = key;
        this.label = label;
        this.classId = classId;
        this.registry = registry;
    }

    @Override
    public T create() {
        return registry.create(classId);
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
