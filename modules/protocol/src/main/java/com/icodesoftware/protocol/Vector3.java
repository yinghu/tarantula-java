package com.icodesoftware.protocol;

/**
 * Created by yinghu lu on 12/14/2020.
 */
public class Vector3 {
    public float x;
    public float y;
    public float z;
    public Vector3(float x,float y, float z){
        this.x = x;
        this.y = y;
        this.z = z;
    }
    @Override
    public String toString(){
        return "x:"+x+"y:"+y+"z:"+z;
    }
}
