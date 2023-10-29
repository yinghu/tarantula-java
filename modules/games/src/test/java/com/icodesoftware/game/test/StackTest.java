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
                if(tiles[2].rank>100){
                    stack.swap(tiles);
                    Arrays.sort(tiles,comparator);
                    swap++;
                }
                int mid = (tiles[0].rank+tiles[2].rank)/2;
                int diff = tiles[2].rank-tiles[0].rank;
                if(mid==tiles[1].rank && diff==2)
                {
                    chow++;
                    //print(tiles);
                }
                if(tiles[0].rank==tiles[1].rank && tiles[1]==tiles[2]){
                    pong++;
                    //print(tiles);
                }
                if(tiles[0].rank==tiles[1].rank || tiles[1]==tiles[2]){
                    eye++;
                    //print(tiles);
                }
                Assert.assertNotNull(tiles[0]);
                Assert.assertNotNull(tiles[1]);
                Assert.assertNotNull(tiles[2]);
            }
        }
        System.out.println("Chow->"+(chow/3000d)*100);
        System.out.println("Pong->"+(pong/3000d)*100);
        System.out.println("Pong->"+(eye/3000d)*100);
        System.out.println("Swap->"+(swap/3000d)*100);
    }

    @Test(groups = { "stack" })
    public void stackSequenceTest() {
        Stack stack = new Stack();
        stack.shuffle();
        Tile[] h1 = new Tile[14];
        stack.draw(h1);
        Arrays.sort(h1,new TitleComparator());
        print(h1);
    }
    private void print(Tile[] tiles){
        int line = 3;
        for(int i=0;i< tiles.length;i++){
            System.out.print(tiles[i]);
            line--;
            if(line==0){
                System.out.println();
                line = 3;
            }
        }
        System.out.println();
    }

}
