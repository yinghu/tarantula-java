package com.icodesoftware.game.test;

import com.icodesoftware.game.mahjong.Stack;
import com.icodesoftware.game.mahjong.Tile;
import com.icodesoftware.game.mahjong.TitleComparator;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;

public class StackTest {

    @BeforeClass
    public void setUp() {
    }

    @Test(groups = { "stack" })
    public void stackInitTest() {
        Stack stack = new Stack();
        //stack.shuffle();
        TitleComparator comparator = new TitleComparator();
        int chow = 0;
        int pong = 0;
        int eye = 0;
        int swap = 0;
        for(int r=0;r<100;r++){
            stack.shuffle();
            for(int i=0;i<30;i++){
               Tile[] tiles = stack.draw();

                Arrays.sort(tiles,comparator);
                if(tiles[2].sequence>100){
                    stack.swap(tiles);
                    Arrays.sort(tiles,comparator);
                    swap++;
                }
                int mid = (tiles[0].sequence+tiles[2].sequence)/2;
                int diff = tiles[2].sequence-tiles[0].sequence;
                if(mid==tiles[1].sequence && diff==2)
                {
                    chow++;
                    //print(tiles);
                }
                if(tiles[0].sequence==tiles[1].sequence && tiles[1]==tiles[2]){
                    pong++;
                    //print(tiles);
                }
                if(tiles[0].sequence==tiles[1].sequence || tiles[1]==tiles[2]){
                    eye++;
                    //print(tiles);
                }
                Assert.assertNotNull(tiles[0]);
                Assert.assertNotNull(tiles[1]);
                Assert.assertNotNull(tiles[2]);
            }
        }
        //System.out.println("Chow->"+(chow/3000d)*100);
        //System.out.println("Pong->"+(pong/3000d)*100);
        //System.out.println("Pong->"+(eye/3000d)*100);
        //System.out.println("Swap->"+(swap/3000d)*100);
    }
    private void print(Tile[] tiles){
        System.out.print(tiles[0]);
        System.out.print(tiles[1]);
        System.out.println(tiles[2]);
    }

}
