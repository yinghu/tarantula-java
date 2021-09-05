package com.tarantula.platform.inventory;

import com.tarantula.platform.IndexSet;

public class Inventory extends IndexSet {

    public Inventory(String category){
        this.label = "Inventory/"+category;
    }
    public void setup(){

    }
}