package com.tarantula.test;

import com.tarantula.game.GameRating;
import com.tarantula.platform.presence.pvp.PVPPointGenerator;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PVPPointGeneratorTest {

    @Test(groups = { "PVP_ELO" })
    public void basicGenerateByWinTest(){
        GameRating attackerRating = new GameRating();
        GameRating defenderRating = new GameRating();
        attackerRating.level = 200;
        defenderRating.level = 200;

        int attackerPower = 60;
        int defenderPower = 60;
        boolean attackerWin = true;

        PVPPointGenerator.updateELO(attackerRating, defenderRating, attackerPower, defenderPower, attackerWin);

        Assert.assertEquals(attackerRating.level, 210);
        Assert.assertEquals(defenderRating.level, 198);
    }

    @Test(groups = { "PVP_ELO" })
    public void basicGenerateByLoseTest(){
        GameRating attackerRating = new GameRating();
        GameRating defenderRating = new GameRating();
        attackerRating.level = 200;
        defenderRating.level = 200;

        int attackerPower = 60;
        int defenderPower = 60;
        boolean attackerWin = false;

        PVPPointGenerator.updateELO(attackerRating, defenderRating, attackerPower, defenderPower, attackerWin);

        Assert.assertEquals(attackerRating.level, 190);
        Assert.assertEquals(defenderRating.level, 203);
    }

    @Test(groups = { "PVP_ELO" })
    public void consecutiveWinTest(){
        GameRating attackerRating = new GameRating();
        GameRating defenderRating = new GameRating();
        attackerRating.level = 300;
        defenderRating.level = 300;

        int attackerPower = 60;
        int defenderPower = 60;
        boolean attackerWin = true;

        for (int i = 0; i < 100; i++){
            int oldAttackerELO = attackerRating.level;
            int oldDefenderELO = defenderRating.level;

            PVPPointGenerator.updateELO(attackerRating, defenderRating, attackerPower, defenderPower, attackerWin);

            Assert.assertNotSame(oldAttackerELO, attackerRating.level);
            Assert.assertEquals(oldDefenderELO, defenderRating.level);

        }
    }

    @Test(groups = { "PVP_ELO" })
    public void consecutiveLoseTest(){
        GameRating attackerRating = new GameRating();
        GameRating defenderRating = new GameRating();
        attackerRating.level = 300;
        defenderRating.level = 300;

        int attackerPower = 60;
        int defenderPower = 60;
        boolean attackerWin = false;

        for (int i = 0; i < 100; i++){
            int oldAttackerELO = attackerRating.level;
            int oldDefenderELO = defenderRating.level;

            PVPPointGenerator.updateELO(attackerRating, defenderRating, attackerPower, defenderPower, attackerWin);

            Assert.assertEquals(oldAttackerELO, attackerRating.level);
            Assert.assertNotSame(oldDefenderELO, defenderRating.level);

        }
    }

    @Test(groups = { "PVP_ELO" })
    public void howManyConsecutiveWinsOver2500(){
        GameRating attackerRating = new GameRating();
        GameRating defenderRating = new GameRating();
        attackerRating.level = 300;
        defenderRating.level = 300;

        int attackerPower = 60;
        int defenderPower = 60;
        boolean attackerWin = true;

        for (int i = 0; i < 100000; i++){
            PVPPointGenerator.updateELO(attackerRating, defenderRating, attackerPower, defenderPower, attackerWin);
            if(attackerRating.level > 2500){
                Assert.assertTrue(true, "Wins to 2500: " + i);
                return;
            }
        }
        Assert.fail("Never hit 2500 ELO");
    }

    @Test(groups = { "PVP_ELO" })
    public void howManyConsecutiveLostTo300(){
        GameRating attackerRating = new GameRating();
        GameRating defenderRating = new GameRating();
        attackerRating.level = 2500;
        defenderRating.level = 2500;

        int attackerPower = 60;
        int defenderPower = 60;
        boolean attackerWin = false;

        for (int i = 0; i < 100000; i++){
            PVPPointGenerator.updateELO(attackerRating, defenderRating, attackerPower, defenderPower, attackerWin);
            if(attackerRating.level == 300){
                Assert.assertTrue(true, "Loses to 300: " + i);
                return;
            }
        }
        Assert.fail("Never hit 300 ELO");
    }

    @Test(groups = { "PVP_ELO" })
    public void capELO300(){
        GameRating attackerRating = new GameRating();
        GameRating defenderRating = new GameRating();
        attackerRating.level = 298;
        defenderRating.level = 290;

        int attackerPower = 60;
        int defenderPower = 60;
        boolean attackerWin = true;

        PVPPointGenerator.updateELO(attackerRating, defenderRating, attackerPower, defenderPower, attackerWin);

        Assert.assertEquals(attackerRating.level, 308);
        Assert.assertEquals(defenderRating.level, 287);

        attackerWin = false;

        PVPPointGenerator.updateELO(attackerRating, defenderRating, attackerPower, defenderPower, attackerWin);

        Assert.assertEquals(attackerRating.level, 300);
        Assert.assertEquals(defenderRating.level, 289);

        PVPPointGenerator.updateELO(attackerRating, defenderRating, attackerPower, defenderPower, attackerWin);

        Assert.assertEquals(attackerRating.level, 300);
        Assert.assertEquals(defenderRating.level, 291);

        PVPPointGenerator.updateELO(attackerRating, defenderRating, attackerPower, defenderPower, attackerWin);

        Assert.assertEquals(attackerRating.level, 300);
        Assert.assertEquals(defenderRating.level, 293);
    }
}
