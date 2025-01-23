package com.tarantula.platform.presence.pvp;


import com.icodesoftware.Rating;
import com.tarantula.game.GameRating;

//ELO implementation
public class PVPPointGenerator {
    public static int softMaxRatingChange = 20;
    public static int softDefenseMaxRatingChange = 5;
    public static int powerDiffMaxRating = 1500;
    public static int minPowerDiffConstant = 1000;
    public static int maxPowerDiffConstant = 2000;
    public static int eloExponent = 800;
    public static int relativePowerConstant = maxPowerDiffConstant - minPowerDiffConstant;

    public static int update(Rating rating, boolean win){

        return 0; //new level
    }

    public static void updateELO(GameRating attackerRating, GameRating defenderRating, int attackerPower, int defenderPower, boolean attackerWin){
        double powerDifference = attackerPower - defenderPower;
        double ratingDifference = attackerRating.level() - defenderRating.level();

        double probOfWinAttacker = 1.0 / (1.0 + Math.pow(10, ratingDifference /eloExponent));
        double probOfWinDefender = 1.0 - probOfWinAttacker;

        double attackerPowerModifier = (Math.min(relativePowerConstant, Math.max(0, Math.abs(powerDifference) - minPowerDiffConstant)) / relativePowerConstant) * (attackerRating.level < powerDiffMaxRating ? 1 : 0);
        double defenderPowerModifier = 1 - attackerPowerModifier;

        double maxRatingChangeAttacker = softMaxRatingChange * (attackerWin ? (1 + attackerPowerModifier) : 1);
        double maxRatingChangeDefender = softDefenseMaxRatingChange * (!attackerWin ? defenderPowerModifier : 1);

        attackerRating.level = (int) Math.round(attackerRating.level() + maxRatingChangeAttacker * ((attackerWin ? 1 : 0) - probOfWinAttacker));
        defenderRating.level = (int) Math.round(defenderRating.level() + maxRatingChangeDefender * ((!attackerWin ? 1 : 0) - probOfWinDefender));
    }
}
