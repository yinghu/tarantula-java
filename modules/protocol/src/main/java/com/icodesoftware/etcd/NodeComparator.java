package com.icodesoftware.etcd;

import java.util.Comparator;

public class NodeComparator implements Comparator<EtcdNode> {

    @Override
    public int compare(EtcdNode o1, EtcdNode o2) {
        return o1.name().compareTo(o2.name());
    }
}
