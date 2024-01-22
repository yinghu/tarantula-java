package com.icodesoftware.game.test;

import com.icodesoftware.game.mahjong.Stack;
import com.icodesoftware.game.mahjong.Tile;
import com.icodesoftware.game.mahjong.TitleComparator;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class StackTest {

    @BeforeClass
    public void setUp() {
    }

    @Test(groups = { "stack" })
    public void stackSetupTest() {
        for(int size = 1;size<4;size++){
            Stack stack = Stack.stack(size);
            stack.shuffle(6);
            Tile[] h1 = new Tile[14];
            Assert.assertTrue(stack.draw(h1));
            int[] debug = stack.debug();
            Assert.assertEquals(debug.length,size+1);
            Arrays.sort(h1,new TitleComparator());
            for(int i=1;i<14;i++){
                Assert.assertTrue(h1[i].rank>=h1[i-1].rank);
            }
        }
    }
    @Test(groups = { "stack" })
    public void drawTest() {
        Stack stack = Stack.stack(3);
        stack.shuffle(3);
        Tile[] h1 = new Tile[1];
        Assert.assertTrue(stack.draw(h1));
        Assert.assertNotNull(h1[0]);

        Tile[] h3 = new Tile[3];
        Assert.assertTrue(stack.draw(h3));
        for(Tile t : h3){
            Assert.assertNotNull(t);
        }

        Tile[] h10 = new Tile[10];
        Assert.assertTrue(stack.draw(h10));
        for(Tile t : h10){
            Assert.assertNotNull(t);
        }
    }
    @Test(groups = { "stack" })
    public void swapTest() {
        Stack stack = Stack.stack(1);
        stack.shuffle(3);
        Assert.assertNotNull(stack.swap(Tile.F1));
        Assert.assertNotNull(stack.swap(new Tile[]{Tile.B1,Tile.B1,Tile.B1,Tile.B1}));
        Exception exception = null;
        try{
            stack.swap(Tile.B1);
        }catch (Exception ex){
            exception = ex;
        }
        Assert.assertNotNull(exception);
        exception = null;
        try{
            stack.swap(new Tile[0]);
        }catch (Exception ex){
            exception = ex;
        }
        Assert.assertNotNull(exception);
    }

}
