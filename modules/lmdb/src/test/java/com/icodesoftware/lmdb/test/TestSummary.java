package com.icodesoftware.lmdb.test;

import com.icodesoftware.service.DataStoreSummary;

import java.util.List;

public class TestSummary implements DataStoreSummary {

    private long count;
    private int depth;
    private int pageSize;

    private long leafPages;
    private long branchPages;
    private long overflowPages;

    private List<String> edgeList;

    @Override
    public String name() {
        return "";
    }

    @Override
    public int scope() {
        return 0;
    }

    @Override
    public long count() {
        return count;
    }

    @Override
    public int depth() {
        return depth;
    }

    @Override
    public int pageSize() {
        return pageSize;
    }

    @Override
    public long leafPages() {
        return leafPages;
    }

    @Override
    public long overflowPages() {
        return overflowPages;
    }

    @Override
    public long branchPages() {
        return branchPages;
    }

    @Override
    public List<String> edgeList() {
        return edgeList;
    }

    @Override
    public void list(View view) {

    }

    @Override
    public void load(byte[] key, View view) {

    }

    @Override
    public void count(long count) {
        this.count = count;
    }

    @Override
    public void depth(int depth) {
        this.depth = depth;
    }

    @Override
    public void pageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    @Override
    public void leafPages(long leafPages) {
        this.leafPages = leafPages;
    }

    @Override
    public void overflowPages(long overflowPages) {
        this.overflowPages = overflowPages;
    }

    @Override
    public void branchPages(long branchPages) {
        this.branchPages = branchPages;
    }

    @Override
    public void edgeList(List<String> edgeList) {
        this.edgeList = edgeList;
    }
}
