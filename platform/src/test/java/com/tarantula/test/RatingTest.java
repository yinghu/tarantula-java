package com.tarantula.test;

import com.tarantula.game.Rating;
import com.tarantula.platform.configuration.AWSSigner;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RatingTest {


    @Test(groups = { "Rating" })
    public void levelUp1000Test() {
        Rating rating = new Rating();
        double levelUpLimit = 1000;
        Rating r = rating.update(10,levelUpLimit);
        Assert.assertEquals(r.rank, 1);
        Assert.assertEquals(r.xp,10d);
        Assert.assertEquals(r.level,1);
        for(int i=1;i<=99;i++) {
            r.update(levelUpLimit,levelUpLimit);
            Assert.assertEquals(r.rank, 1);
            Assert.assertEquals(r.xp, i*levelUpLimit+10d);
            Assert.assertEquals(r.level,i+1);
        }
        r.update(levelUpLimit,levelUpLimit);
        Assert.assertEquals(r.rank, 2);
        Assert.assertEquals(r.xp, 100*levelUpLimit+10d);
        Assert.assertEquals(r.level,101);
    }

    @Test(groups = { "Rating" })
    public void levelUp500Test() {
        Rating rating = new Rating();
        double levelUpLimit = 500;
        Rating r = rating.update(10,levelUpLimit);
        Assert.assertEquals(r.rank, 1);
        Assert.assertEquals(r.xp,10d);
        Assert.assertEquals(r.level,1);
        for(int i=1;i<=99;i++) {
            r.update(levelUpLimit,levelUpLimit);
            Assert.assertEquals(r.rank, 1);
            Assert.assertEquals(r.xp, i*levelUpLimit+10d);
            Assert.assertEquals(r.level,i+1);
        }
        r.update(levelUpLimit,levelUpLimit);
        Assert.assertEquals(r.rank, 2);
        Assert.assertEquals(r.xp, 100*levelUpLimit+10d);
        Assert.assertEquals(r.level,101);

    }

    @Test(groups = { "Rating" })
    public void levelUp200Test() {
        Rating rating = new Rating();
        double levelUpLimit = 200;
        Rating r = rating.update(10,levelUpLimit);
        Assert.assertEquals(r.rank, 1);
        Assert.assertEquals(r.xp,10d);
        Assert.assertEquals(r.level,1);
        for(int i=1;i<=99;i++) {
            r.update(levelUpLimit,levelUpLimit);
            Assert.assertEquals(r.rank, 1);
            Assert.assertEquals(r.xp, i*levelUpLimit+10d);
            Assert.assertEquals(r.level,i+1);
        }
        r.update(levelUpLimit,levelUpLimit);
        Assert.assertEquals(r.rank, 2);
        Assert.assertEquals(r.xp, 100*levelUpLimit+10d);
        Assert.assertEquals(r.level,101);
    }
}
