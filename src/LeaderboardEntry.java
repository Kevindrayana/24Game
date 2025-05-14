public class LeaderboardEntry {
    private int rank;
    private String username;
    private int wins;
    private int gamesPlayed;
    private double averageTimeToWin;

    public LeaderboardEntry(int rank, String username, int wins, int gamesPlayed, double averageTimeToWin) {
        this.rank = rank;
        this.username = username;
        this.wins = wins;
        this.gamesPlayed = gamesPlayed;
        this.averageTimeToWin = averageTimeToWin;
    }

    public int getRank() {
        return rank;
    }

    public String getUsername() {
        return username;
    }

    public int getWins() {
        return wins;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public double getAverageTimeToWin() {
        return averageTimeToWin;
    }
}