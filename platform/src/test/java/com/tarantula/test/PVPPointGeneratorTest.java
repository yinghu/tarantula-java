package com.tarantula.test;

import com.tarantula.game.GameRating;
import com.tarantula.platform.presence.pvp.PVPPointGenerator;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PVPPointGeneratorTest {

    @Test(groups = { "PVP_ELO" })
    public void calculateELO(){
        GameRating attackerRating = new GameRating();
        GameRating defenderRating = new GameRating();
        attackerRating.level = 298;
        defenderRating.level = 290;

        int attackerPower = 60;
        int defenderPower = 60;
        boolean attackerWin = true;

        PVPPointGenerator.updateELO(attackerRating, defenderRating, attackerPower, defenderPower, attackerWin);

        Assert.assertEquals(attackerRating.level, 315);
        Assert.assertEquals(defenderRating.level, 287);

        attackerWin = false;

        PVPPointGenerator.updateELO(attackerRating, defenderRating, attackerPower, defenderPower, attackerWin);

        Assert.assertEquals(attackerRating.level, 306);
        Assert.assertEquals(defenderRating.level, 289);

        PVPPointGenerator.updateELO(attackerRating, defenderRating, attackerPower, defenderPower, attackerWin);

        Assert.assertEquals(attackerRating.level, 300);
        Assert.assertEquals(defenderRating.level, 291);
    }
}
