package com.tarantula.test.integration;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


public class LoadResult {


    static AtomicInteger totalSuccessRegister = new AtomicInteger(0);
    static AtomicInteger totalFailureRegister = new AtomicInteger(0);

    static AtomicInteger totalSuccessLogin = new AtomicInteger(0);
    static AtomicInteger totalFailureLogin = new AtomicInteger(0);

    static AtomicInteger totalSuccessLeave = new AtomicInteger(0);
    static AtomicInteger totalFailureLeave = new AtomicInteger(0);

    static AtomicInteger totalSuccessJoin = new AtomicInteger(0);
    static AtomicInteger totalFailureJoin = new AtomicInteger(0);

    static AtomicInteger totalSuccessPlay = new AtomicInteger(0);
    static AtomicInteger totalFailurePlay = new AtomicInteger(0);

    static AtomicInteger totalSuccessStatistics = new AtomicInteger(0);
    static AtomicInteger totalFailureStatistics = new AtomicInteger(0);

    static AtomicInteger totalSuccessCreateProfile = new AtomicInteger(0);
    static AtomicInteger totalFailureCreateProfile = new AtomicInteger(0);

    static AtomicInteger totalSuccessUpdateGame = new AtomicInteger(0);
    static AtomicInteger totalFailureUpdateGame = new AtomicInteger(0);

    static AtomicInteger totalSuccessStartGame = new AtomicInteger(0);
    static AtomicInteger totalFailureStartGame = new AtomicInteger(0);

    static AtomicInteger totalSuccessScoreTournament = new AtomicInteger(0);
    static AtomicInteger totalFailureScoreTournament = new AtomicInteger(0);

    static AtomicInteger totalSuccessEndGame = new AtomicInteger(0);
    static AtomicInteger totalFailureEndGame = new AtomicInteger(0);

    static AtomicInteger totalSuccessFetchProfile = new AtomicInteger(0);
    static AtomicInteger totalFailureFetchProfile = new AtomicInteger(0);

    static AtomicInteger totalSuccessSaveOnSet = new AtomicInteger(0);
    static AtomicInteger totalFailureSaveOnSet = new AtomicInteger(0);

    static AtomicInteger totalSuccessSaveOnGet = new AtomicInteger(0);
    static AtomicInteger totalFailureSaveOnGet = new AtomicInteger(0);

    static AtomicInteger totalSuccessLoadShop = new AtomicInteger(0);
    static AtomicInteger totalFailureLoadShop = new AtomicInteger(0);

    static AtomicInteger totalSuccessLoadTournament = new AtomicInteger(0);
    static AtomicInteger totalFailureLoadTournament = new AtomicInteger(0);

    static AtomicInteger totalSuccessLoadTournamentRaceBoard = new AtomicInteger(0);
    static AtomicInteger totalFailureLoadTournamentRaceBoard = new AtomicInteger(0);

    static AtomicInteger totalSuccessOnGameEvent = new AtomicInteger(0);
    static AtomicInteger totalFailureOnGameEvent = new AtomicInteger(0);

    static AtomicInteger totalFailureOther = new AtomicInteger(0);

    static AtomicLong totalUDPBytesSent = new AtomicLong(0);
    static AtomicLong totalUDPBytesReceived = new AtomicLong(0);

    static AtomicInteger totalSuccessUDPSent = new AtomicInteger(0);
    static AtomicInteger totalSuccessUDPReceived = new AtomicInteger(0);

    static AtomicInteger totalUDPReceiveTimeout = new AtomicInteger(0);


    static AtomicInteger totalRounds = new AtomicInteger(0);

    static AtomicInteger totalHttpRequestCount = new AtomicInteger(0);

    static AtomicLong totalHttpRequestTime = new AtomicLong(0);

    static AtomicLong totalUDPSentTime = new AtomicLong(0);
    static AtomicLong totalUDPReceiveTime = new AtomicLong(0);

    static int batch;
    static int poolSize;

    static String host;

    static boolean udpTested;
    static int udpReceiveTimeout;
    static int udpTestRounds;
    static String playerPrefix;
    static LocalDateTime startTime;


    public static void print(boolean onFile){

            LocalDateTime localDateTime = LocalDateTime.now();
            try(OutputStream fos = onFile?new FileOutputStream(localDateTime.format(DateTimeFormatter.ISO_DATE_TIME)+".txt"):System.out;
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos))
            ){
                bw.write("###### Load Test Summary ######\n");
                bw.write("Server Host ["+host+"]\n");
                bw.write("Batch Size ["+batch+"]\n");
                bw.write("Pool Size ["+poolSize+"]\n");
                bw.write("Player Prefix ["+playerPrefix+"]\n");
                bw.write("Test UDP Enabled ["+udpTested+"]\n");
                bw.write("Test UDP Receive Timeout ["+udpReceiveTimeout+"]\n");
                bw.write("Test UDP Rounds ["+udpTestRounds+"]\n");
                bw.write("Start time ["+startTime.format(DateTimeFormatter.ISO_DATE_TIME)+"]\n");
                bw.write("End time ["+localDateTime.format(DateTimeFormatter.ISO_DATE_TIME)+"]\n\n");

                bw.write("###### HTTP Operation Summary ######\n");
                bw.write("Total Rounds ["+totalRounds.get()+"]\n");
                bw.write("Total Failure Other ["+totalFailureOther.get()+"]\n");
                bw.write("Total Success Register Count ["+totalSuccessRegister.get()+"]\n");
                bw.write("Total Failure Register Count ["+totalFailureRegister.get()+"]\n");
                bw.write("Total Success Login Count ["+totalSuccessLogin.get()+"]\n");
                bw.write("Total Failure Login Count ["+totalFailureLogin.get()+"]\n");
                bw.write("Total Success Join Count ["+totalSuccessJoin.get()+"]\n");
                bw.write("Total Failure Join Count ["+totalFailureJoin.get()+"]\n");
                bw.write("Total Success Statistics Count ["+totalSuccessStatistics.get()+"]\n");
                bw.write("Total Failure Statistics Count ["+totalFailureStatistics.get()+"]\n");
                bw.write("Total Success CreateProfile Count ["+totalSuccessCreateProfile.get()+"]\n");
                bw.write("Total Failure CreateProfile Count ["+totalFailureCreateProfile.get()+"]\n");
                bw.write("Total Success FetchProfile Count ["+totalSuccessFetchProfile.get()+"]\n");
                bw.write("Total Failure FetchProfile Count ["+totalFailureFetchProfile.get()+"]\n");
                bw.write("Total Success SaveOnSet Count ["+totalSuccessSaveOnSet.get()+"]\n");
                bw.write("Total Failure SaveOnSet Count ["+totalFailureSaveOnSet.get()+"]\n");
                bw.write("Total Success SaveOnGet Count ["+totalSuccessSaveOnGet.get()+"]\n");
                bw.write("Total Failure SaveOnGet Count ["+totalFailureSaveOnGet.get()+"]\n");
                bw.write("Total Success LoadShop Count ["+totalSuccessLoadShop.get()+"]\n");
                bw.write("Total Failure LoadShop Count ["+totalFailureLoadShop.get()+"]\n");
                bw.write("Total Success LoadTournament Count ["+totalSuccessLoadTournament.get()+"]\n");
                bw.write("Total Failure LoadTournament Count ["+totalFailureLoadTournament.get()+"]\n");
                bw.write("Total Success LoadTournamentRaceBoard Count ["+totalSuccessLoadTournamentRaceBoard.get()+"]\n");
                bw.write("Total Failure LoadTournamentRaceBoard Count ["+totalFailureLoadTournamentRaceBoard.get()+"]\n");
                bw.write("Total Success StartGame Count ["+totalSuccessStartGame.get()+"]\n");
                bw.write("Total Failure StartGame Count ["+totalFailureStartGame.get()+"]\n");
                bw.write("Total Success ScoreTournament Count ["+totalSuccessScoreTournament.get()+"]\n");
                bw.write("Total Failure ScoreTournament Count ["+totalFailureScoreTournament.get()+"]\n");
                bw.write("Total Success EndGame Count ["+totalSuccessEndGame.get()+"]\n");
                bw.write("Total Failure EndGame Count ["+totalFailureEndGame.get()+"]\n");
                bw.write("Total Success UpdateGame Count ["+totalSuccessUpdateGame.get()+"]\n");
                bw.write("Total Failure UpdateGame Count ["+totalFailureUpdateGame.get()+"]\n");
                bw.write("Total Success OnGameEvent Count ["+totalSuccessOnGameEvent.get()+"]\n");
                bw.write("Total Failure OnGameEvent Count ["+totalFailureOnGameEvent.get()+"]\n");
                bw.write("Total Success Leave Count ["+totalSuccessLeave.get()+"]\n");
                bw.write("Total Failure Leave Count ["+totalFailureLeave.get()+"]\n");
                bw.write("Total Http Request Count ["+totalHttpRequestCount.get()+"]\n");
                bw.write("Average HTTP Request Duration (ms) ["+(totalHttpRequestTime.get()/totalHttpRequestCount.get())+"]\n\n");

                //bw.write("###### UDP Operation Summary ######\n");
                //bw.write("Total Success Play Count ["+totalSuccessPlay.get()+"]\n");
                //bw.write("Total Failure Play Count ["+totalFailurePlay.get()+"]\n");

                //bw.write("Total UDP Receive Duration (ms) ["+(totalUDPReceiveTime.get())+"]\n");
                //bw.write("Total Success UDP Received Count ["+totalSuccessUDPReceived.get()+"]\n");
                //bw.write("Total UDP Sent Duration (ms) ["+(totalUDPSentTime.get())+"]\n");
                //bw.write("Total Success UDP Sent Count ["+totalSuccessUDPSent.get()+"]\n");

                //bw.write("Total Bytes UDP Sent ["+totalUDPBytesSent.get()+"]\n");
                //bw.write("Total Bytes UDP Received ["+totalUDPBytesReceived.get()+"]\n");
                //bw.write("Total UDP Receive Timeout Count ["+totalUDPReceiveTimeout.get()+"]\n");
            } catch (Exception ex){
                ex.printStackTrace();
            }
    }
}

