package com.icodesoftware.game.test;

import com.icodesoftware.game.Dice;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DiceTest {

    @BeforeClass
    public void setUp() {
    }

    @Test(groups = { "dice" })
    public void diceRollTest() {
        Dice dice =Dice.dice(2);
        for(int i=0;i<1000;i++){
            int[] pts = dice.roll();
            Assert.assertTrue(pts[0]>=1&pts[0]<=6);
            Assert.assertTrue(pts[1]>=1&pts[1]<=6);
        }
    }

}
