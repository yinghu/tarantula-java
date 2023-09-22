package com.icodesoftware.service;

import com.icodesoftware.Recoverable;

import java.util.List;

public interface DataStoreSummary {

    String name();

    int scope();
    long count();
    int depth();
    int pageSize();

    long leafPages();

    long overflowPages();

    long branchPages();

    List<String> edgeList();

    void list(DataStoreSummary.View view);

    void load(byte[] key, DataStoreSummary.View view);

    void count(long count);
    void depth(int depth);

    void pageSize(int pageSize);

    void leafPages(long leafPages);

    void overflowPages(long overflowPages);

    void branchPages(long branchPages);

    void edgeList(List<String> edgeList);
    interface View{
         boolean on(ClusterProvider.Node node,Recoverable.DataHeader h, Recoverable t);
    }
}
