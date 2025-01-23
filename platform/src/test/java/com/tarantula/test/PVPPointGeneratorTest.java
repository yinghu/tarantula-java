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
        attackerRating.level = 20;
        defenderRating.level = 15;

        int attackerPower = 50;
        int defenderPower = 60;
        boolean attackerWin = true;

        PVPPointGenerator.updateELO(attackerRating, defenderRating, attackerPower, defenderPower, attackerWin);

        Assert.assertEquals(attackerRating.level, 30);
        Assert.assertEquals(defenderRating.level, 12);


        attackerRating.level = 45;
        defenderRating.level = 45;
        attackerPower = 100;
        defenderPower = 70;
        attackerWin = false;

        PVPPointGenerator.updateELO(attackerRating, defenderRating, attackerPower, defenderPower, attackerWin);

        Assert.assertEquals(attackerRating.level, 35);
        Assert.assertEquals(defenderRating.level, 48);
    }
}
