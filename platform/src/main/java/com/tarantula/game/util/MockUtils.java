package com.tarantula.game.util;

import com.icodesoftware.Tournament;
import com.tarantula.platform.tournament.TournamentEntry;
import com.tarantula.platform.tournament.TournamentRanking;

import java.util.ArrayList;
import java.util.List;

public class MockUtils {
    public static List<TournamentRanking> GetMockTournamentRankings(long playerId) {
        List<TournamentRanking> result = new ArrayList<>();
        result.add(GetMockStandardTournament(0, playerId));
        result.add(GetMockStandardTournament(1, playerId));
        result.add(GetMockStandardTournament(2, playerId));
        result.add(GetMockUpperRankingTournament(3, playerId));
        return result;
    }

    public static TournamentRanking GetMockStandardTournament(long tournamentId, long playerId) {
        List<Tournament.Entry> topTen = new ArrayList<>();
        for (var i = 0; i < 10; i++) {
            topTen.add(new TournamentEntry(i, 100 - i, i,"TestPlayerName", 0));
        }

        List<Tournament.Entry> playerRankings = new ArrayList<>();
        for (var i = 0; i < 5; i++) {
            var idOffset = i + 50;
            playerRankings.add(new TournamentEntry(idOffset, 50 - i, idOffset, "TestPlayerName", 0));
        }
        playerRankings.add(new TournamentEntry(playerId, 30, 55, "Player", 0));
        playerRankings.add(new TournamentEntry(56, 30, 56, "TestPlayerName", 0));

        return new TournamentRanking(tournamentId, topTen, playerRankings);
    }

    public static TournamentRanking GetMockUpperRankingTournament(long tournamentId, long playerId) {
        List<Tournament.Entry> topTen = new ArrayList<>();
        for (var i = 0; i < 10; i++) {
            topTen.add(new TournamentEntry(i == 3 ? playerId : i, 100 - i, i, "TestPlayerName", 0));
        }

        List<Tournament.Entry> playerRankings = new ArrayList<>();
        for (var i = 0; i < 5; i++) {
            playerRankings.add(new TournamentEntry(i == 3 ? playerId : i, 100 - i, i, i == 3 ? "Player" : "TestPlayerName", 0));
        }

        return new TournamentRanking(tournamentId, topTen, playerRankings);
    }
}
