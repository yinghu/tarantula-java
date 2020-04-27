package com.tarantula.platform.service.rating;

import com.tarantula.game.Stub;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.service.ServiceContext;
import com.tarantula.platform.service.ServiceProvider;

/**
 * pxp - performance xp percentage on 100 base points pxp*(100) 0.7*100 = 70 0.3*100 = 30
 * rank - final result 1,2 rank xp = (1/rank)*100  1 - 100 2 50 ..
 * xp-delta = (1/rank)*(100)+pxp*(100)+csw*(100); //cws only if last is cws
 * zxp = zxp +xp-delta
 * xp = xp + xp-delta
 */
public class RatingServiceProvider implements ServiceProvider {

    private JDKLogger logger = JDKLogger.getLogger(RatingServiceProvider.class);
    private final String NAME;
    private static double BASE_POINTS = 100;
    private static int ELO_K = 30;
    public RatingServiceProvider(String name){
        NAME = name;
    }
    public Rating rating(Stub stub){
        Rating rating = new Rating();
        double dxp = (1/stub.rank+stub.pxp)*BASE_POINTS;
        rating.zxp += dxp;
        rating.xp += dxp;
        return rating;
    }
    public Rating rating(String systemId){
        return new Rating();
    }
    public void elo(Rating rating1,Rating rating2){

    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        logger.warn("set up rating service->"+NAME);
        serviceContext.dataStore(NAME,serviceContext.partitionNumber());
    }

    @Override
    public void waitForData() {

    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }
    static double Probability(double rating1,
                             double rating2)
    {
        return 1.0d * 1.0d / (1 + 1.0d *
                (Math.pow(10, 1.0d *
                        (rating1 - rating2) / 400)));
    }
    static void EloRating(double Ra, double Rb,
                          int K, boolean d)
    {

        // To calculate the Winning
        // Probability of Player B
        double Pb = Probability(Ra, Rb);

        // To calculate the Winning
        // Probability of Player A
        double Pa = Probability(Rb, Ra);

        // Case -1 When Player A wins
        // Updating the Elo Ratings
        if (d == true) {
            Ra = Ra + K * (1 - Pa);
            Rb = Rb + K * (0 - Pb);
        }

        // Case -2 When Player B wins
        // Updating the Elo Ratings
        else {
            Ra = Ra + K * (0 - Pa);
            Rb = Rb + K * (1 - Pb);
        }

        System.out.print("Updated Ratings:-\n");

        System.out.print("Ra = " + (Math.round(
                Ra * 1000000.0) / 1000000.0)
                + " Rb = " + Math.round(Rb
                * 1000000.0) / 1000000.0);
    }
}
