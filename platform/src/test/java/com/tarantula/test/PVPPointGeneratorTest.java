package com.tarantula.test;

import com.tarantula.game.GameRating;
import com.tarantula.platform.presence.pvp.PVPPointGenerator;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Random;

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
                Assert.assertTrue(true, "Losses to 300: " + i);
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
        Assert.assertEquals(defenderRating.level, 288);

        attackerWin = false;

        PVPPointGenerator.updateELO(attackerRating, defenderRating, attackerPower, defenderPower, attackerWin);

        Assert.assertEquals(attackerRating.level, 300);
        Assert.assertEquals(defenderRating.level, 291);

        PVPPointGenerator.updateELO(attackerRating, defenderRating, attackerPower, defenderPower, attackerWin);

        Assert.assertEquals(attackerRating.level, 300);
        Assert.assertEquals(defenderRating.level, 294);

        PVPPointGenerator.updateELO(attackerRating, defenderRating, attackerPower, defenderPower, attackerWin);

        Assert.assertEquals(attackerRating.level, 300);
        Assert.assertEquals(defenderRating.level, 297);
    }

    @Test(groups = { "PVP_ELO" })
    public void ELOChangeAtLeastOne(){
        GameRating attackerRating = new GameRating();
        GameRating defenderRating = new GameRating();
        attackerRating.level = 550;
        defenderRating.level = 1;

        int attackerPower = 60;
        int defenderPower = 60;
        boolean attackerWin = true;

        int oldAttackerELO = attackerRating.level;
        int oldDefenderELO = defenderRating.level;

        PVPPointGenerator.updateELO(attackerRating, defenderRating, attackerPower, defenderPower, attackerWin);

        Assert.assertEquals(attackerRating.level - oldAttackerELO, 1);
        Assert.assertEquals(defenderRating.level - oldDefenderELO, -1);
    }

    @Test(groups = { "PVP_ELO" })
    public void ELONotNegitive(){
        GameRating attackerRating = new GameRating();
        GameRating defenderRating = new GameRating();
        attackerRating.level = 1;
        defenderRating.level = 1;

        int attackerPower = 60;
        int defenderPower = 60;
        boolean attackerWin = true;

        PVPPointGenerator.updateELO(attackerRating, defenderRating, attackerPower, defenderPower, attackerWin);

        Assert.assertEquals(attackerRating.level, 11);
        Assert.assertEquals(defenderRating.level , 0);

        PVPPointGenerator.updateELO(attackerRating, defenderRating, attackerPower, defenderPower, attackerWin);

        Assert.assertEquals(attackerRating.level, 21);
        Assert.assertEquals(defenderRating.level , 0);
    }

    @Test(groups = { "PVP_ELO" })
    public void negativeELOTest(){
        GameRating attackerRating = new GameRating();
        GameRating defenderRating = new GameRating();
        attackerRating.level = 130;
        defenderRating.level = 0;

        int attackerPower = 60;
        int defenderPower = 60;
        boolean attackerWin = false;

        PVPPointGenerator.updateELO(attackerRating, defenderRating, attackerPower, defenderPower, attackerWin);

        Assert.assertEquals(attackerRating.level, 116);
        Assert.assertEquals(defenderRating.level , 3);
    }

    @Test(groups = { "PVP_ELO" })
    public void ZeroELOWinTest(){
        GameRating attackerRating = new GameRating();
        GameRating defenderRating = new GameRating();
        attackerRating.level = 0;
        defenderRating.level = 0;

        int attackerPower = 60;
        int defenderPower = 60;
        boolean attackerWin = true;

        PVPPointGenerator.updateELO(attackerRating, defenderRating, attackerPower, defenderPower, attackerWin);

        Assert.assertEquals(attackerRating.level, 10);
        Assert.assertEquals(defenderRating.level , 0);
    }

    @Test(groups = { "PVP_ELO" })
    public void ZeroELOLoseTest(){
        GameRating attackerRating = new GameRating();
        GameRating defenderRating = new GameRating();
        attackerRating.level = 0;
        defenderRating.level = 0;

        int attackerPower = 60;
        int defenderPower = 60;
        boolean attackerWin = false;

        PVPPointGenerator.updateELO(attackerRating, defenderRating, attackerPower, defenderPower, attackerWin);

        Assert.assertEquals(attackerRating.level, 0);
        Assert.assertEquals(defenderRating.level , 3);
    }

    @Test(groups = { "PVP_ELO" })
    public void basicGenerateByWinTestHighPower(){
        GameRating attackerRating = new GameRating();
        GameRating defenderRating = new GameRating();
        attackerRating.level = 53;
        defenderRating.level = 0;

        int attackerPower = 2146;
        int defenderPower = 6515;
        boolean attackerWin = false;

        PVPPointGenerator.updateELO(attackerRating, defenderRating, attackerPower, defenderPower, attackerWin);
        Assert.assertEquals(attackerRating.level, 41);
        Assert.assertEquals(defenderRating.level , 3);
    }

    @Test(groups = { "PVP_ELO" })
    public void randomTeamStatsTest(){
        GameRating attackerRating = new GameRating();
        GameRating defenderRating = new GameRating();
        Random rand = new Random();

        for (int i = 0; i < 100000; i++){
            attackerRating.level = rand.nextInt(1500);;
            defenderRating.level = rand.nextInt(1500);;

            int attackerPower = rand.nextInt(1500);;
            int defenderPower = rand.nextInt(1500);;
            boolean attackerWin = rand.nextBoolean();

            int oldAttackerELO = attackerRating.level;
            int oldDefenderELO = defenderRating.level;

            PVPPointGenerator.updateELO(attackerRating, defenderRating, attackerPower, defenderPower, attackerWin);

            if(attackerWin){
                if(!(attackerRating.level > oldAttackerELO)){
                    System.out.println("attackWin: " + attackerWin + " oldDefenseELO: " + oldDefenderELO);
                    System.out.println("attackPower: " + attackerPower + " defensePower: " + defenderPower);
                    System.out.println("new: " + attackerRating.level + " old: " + oldAttackerELO);

                    Assert.fail();
                }

                if(!(defenderRating.level < oldDefenderELO || defenderRating.level == 0 || defenderRating.level == 300)){
                    System.out.println("attackWin: " + attackerWin+ " oldAttackELO: " + oldAttackerELO);
                    System.out.println("attackPower: " + attackerPower + " defensePower: " + defenderPower);
                    System.out.println("new: " + defenderRating.level + " old: " + oldDefenderELO);

                    Assert.fail();
                }

            }else{
                if(!(defenderRating.level > oldDefenderELO)){
                    System.out.println("attackWin: " + attackerWin+ " oldAttackELO: " + oldAttackerELO);
                    System.out.println("attackPower: " + attackerPower + " defensePower: " + defenderPower);
                    System.out.println("new: " + defenderRating.level + " old: " + oldDefenderELO);

                    Assert.fail();
                }

                if(!(attackerRating.level < oldAttackerELO || attackerRating.level == 0 || attackerRating.level == 300)){
                    System.out.println("attackWin: " + attackerWin + " oldDefenseELO: " + oldDefenderELO);
                    System.out.println("attackPower: " + attackerPower + " defensePower: " + defenderPower);
                    System.out.println("new: " + attackerRating.level + " old: " + oldAttackerELO);

                    Assert.fail();
                }

            }
        }
    }
}
