package com.tarantula.platform.tournament;

import com.icodesoftware.Tournament;

import java.util.ArrayList;
import java.util.List;

public class TournamentMockUtils {
    public static List<TournamentRanking> GetMockTournamentRankings(long playerId) {
        List<TournamentRanking> result = new ArrayList<>();
        result.add(GetMockStandardTournament("tournament1", playerId));
        result.add(GetMockStandardTournament("tournament2", playerId));
        result.add(GetMockStandardTournament("tournament3", playerId));
        result.add(GetMockUpperRankingTournament("tournament4", playerId));
        return result;
    }

    public static TournamentRanking GetMockStandardTournament(String tournamentType, long playerId) {
        List<Tournament.Entry> topTen = new ArrayList<>();
        for (var i = 0; i < 10; i++) {
            topTen.add(new TournamentEntry(i, 100 - i, i));
        }

        List<Tournament.Entry> playerRankings = new ArrayList<>();
        for (var i = 0; i < 5; i++) {
            var idOffset = i + 50;
            playerRankings.add(new TournamentEntry(idOffset, 50 - i, idOffset));
        }
        playerRankings.add(new TournamentEntry(playerId, 30, 55));
        playerRankings.add(new TournamentEntry(56, 30, 56));

        return new TournamentRanking(tournamentType, topTen, playerRankings);
    }

    public static TournamentRanking GetMockUpperRankingTournament(String tournamentType, long playerId) {
        List<Tournament.Entry> topTen = new ArrayList<>();
        for (var i = 0; i < 10; i++) {
            topTen.add(new TournamentEntry(i == 3 ? playerId : i, 100 - i, i));
        }

        List<Tournament.Entry> playerRankings = new ArrayList<>();
        for (var i = 0; i < 5; i++) {
            playerRankings.add(new TournamentEntry(i == 3 ? playerId : i, 100 - i, i));
        }

        return new TournamentRanking(tournamentType, topTen, playerRankings);
    }
}
