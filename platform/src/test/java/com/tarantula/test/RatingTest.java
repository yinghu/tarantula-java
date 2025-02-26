package com.tarantula.test;

import com.icodesoftware.Rating;
import com.tarantula.game.GameRating;
import org.testng.Assert;
import org.testng.annotations.Test;

public class RatingTest {


    @Test(groups = { "Rating" })
    public void levelUp1000Test() {
        GameRating rating = new GameRating();
        double levelUpLimit = 1000;
        Rating r = rating.update(10,levelUpLimit);
        Assert.assertEquals(r.rank(), 1);
        Assert.assertEquals(r.xp(),10d);
        Assert.assertEquals(r.level(),0);
        /**
        for(int i=1;i<=99;i++) {
            r.update(levelUpLimit,levelUpLimit);
            Assert.assertEquals(r.rank(), 1);
            Assert.assertEquals(r.xp(), i*levelUpLimit+10d);
            Assert.assertEquals(r.level(),i+1);
        }
        r.update(levelUpLimit,levelUpLimit);
        Assert.assertEquals(r.rank(), 2);
        Assert.assertEquals(r.xp(), 100*levelUpLimit+10d);
        Assert.assertEquals(r.level(),101);
    **/
    }

    @Test(groups = { "Rating" })
    public void levelUp500Test() {
        GameRating rating = new GameRating();
        double levelUpLimit = 500;
        Rating r = rating.update(10,levelUpLimit);
        Assert.assertEquals(r.rank(), 1);
        Assert.assertEquals(r.xp(),10d);
        Assert.assertEquals(r.level(),0);
        /**
        for(int i=1;i<=99;i++) {
            r.update(levelUpLimit,levelUpLimit);
            Assert.assertEquals(r.rank(), 1);
            Assert.assertEquals(r.xp(), i*levelUpLimit+10d);
            Assert.assertEquals(r.level(),i+1);
        }
        r.update(levelUpLimit,levelUpLimit);
        Assert.assertEquals(r.rank(), 2);
        Assert.assertEquals(r.xp(), 100*levelUpLimit+10d);
        Assert.assertEquals(r.level(),101);
        **/
    }

    @Test(groups = { "Rating" })
    public void levelUp200Test() {
        GameRating rating = new GameRating();
        double levelUpLimit = 200;
        Rating r = rating.update(10,levelUpLimit);
        Assert.assertEquals(r.rank(), 1);
        Assert.assertEquals(r.xp(),10d);
        Assert.assertEquals(r.level(),0);
        /**
        for(int i=1;i<=99;i++) {
            r.update(levelUpLimit,levelUpLimit);
            Assert.assertEquals(r.rank(), 1);
            Assert.assertEquals(r.xp(), i*levelUpLimit+10d);
            Assert.assertEquals(r.level(),i+1);
        }
        r.update(levelUpLimit,levelUpLimit);
        Assert.assertEquals(r.rank(), 2);
        Assert.assertEquals(r.xp(), 100*levelUpLimit+10d);
        Assert.assertEquals(r.level(),101);**/
    }
}
