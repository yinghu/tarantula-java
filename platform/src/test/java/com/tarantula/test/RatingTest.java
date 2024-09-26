package com.tarantula.test;

import com.icodesoftware.Rating;
import com.icodesoftware.protocol.statistics.UserRating;
import org.testng.Assert;
import org.testng.annotations.Test;

public class RatingTest implements Rating.Listener {


    @Test(groups = { "Rating" })
    public void levelUpTest() {
        UserRating rating = new UserRating();
        Rating r = rating.update(100);
        Assert.assertEquals(r.rank(), 1);
        Assert.assertEquals(r.xp(),100);
        Assert.assertEquals(r.level(),2);
        for(int i=0;i<100;i++) {
            r.update(100);
        }
        Assert.assertEquals(r.rank(), 2);
    }

    @Test(groups = { "Rating" })
    public void levelUpOnListenerTest() {
        UserRating userRating = new UserRating();
        Assert.assertEquals(userRating.level,1);
        Assert.assertEquals(userRating.rank,1);
        Assert.assertEquals(userRating.xp,0);
        for(int i=0;i<10;i++){
            userRating.update(100,this);
        }
        Assert.assertEquals(userRating.level,1+10);
        Assert.assertEquals(userRating.rank,2);
        Assert.assertEquals(userRating.xp,1000);

        for(int i=0;i<10;i++){
            userRating.update(100,this);
        }
        Assert.assertEquals(userRating.level,1+20);
        Assert.assertEquals(userRating.rank,3);
        Assert.assertEquals(userRating.xp,2000);

    }

    @Override
    public boolean levelUp(Rating rating,double xp) {
        return xp>=100;
    }

    @Override
    public boolean rankUp(Rating rating,int level) {
        int _tryRank = 1+((level-1)/10);
        return _tryRank > rating.rank();
    }
}
