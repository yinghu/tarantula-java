package com.tarantula.platform.leveling;


/**
 * Created by yinghu lu on 5/14/2018.
 */
public class XPHeader{
    public String name;
    public String category;

    public XPHeader(String name,String category){
        this.name = name;
        this.category = category;
    }

    @Override
    public int hashCode(){
        return this.name.hashCode()+this.category.hashCode();
    }
    @Override
    public boolean equals(Object obj){
        XPHeader r = (XPHeader) obj;
        return name.equals(r.name)&&category.equals(r.category);
    }
}
