package com.tarantula.test.util;

import com.tarantula.game.Rating;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class RatingUtilTest {

    @BeforeClass
    public void setUp() {
    }

    @Test(groups = { "GameLobby" })
    public void ratingTest() {
        Rating rating = new Rating();
        rating.level = 101;
        Assert.assertEquals(rating.level%100,1);
        rating.level = 102;
        Assert.assertEquals(rating.level%100,2);
    }
}
