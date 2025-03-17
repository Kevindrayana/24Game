public class SqlStatements {
    public static final String INSERT_USERS = "INSERT INTO users (name, password) VALUES (?, ?)";
    public static final String INSERT_ONLINE_USERS = "INSERT INTO online_users (name) VALUES (?)";
    public static final String INSERT_GAMES = "INSERT INTO games (game_id, winner, duration) VALUES (?, ?, ?)";
    public static final String INSERT_USER_TO_GAME = "INSERT INTO user_to_game (user_id, game_id) VALUES (?, ?)";
    public static final String INSERT_USER_TO_GAME_ = "INSERT INTO user_to_game (user_id, game_id) VALUES ((SELECT user_id FROM users WHERE name = ?), ?);";
    public static final String READ_USERS = "SELECT name, password FROM users WHERE name = ?";
    public static final String READ_ONLINE_USERS = "SELECT name FROM online_users WHERE name = ?";
    public static final String READ_USER_WINS = "SELECT COUNT(*) AS wins FROM games WHERE winner = ?";
    public static final String READ_GAMES_PLAYED = "SELECT COUNT(*) AS games_played FROM user_to_game ut JOIN users u ON ut.user_id = u.user_id WHERE u.name = ?";
    public static final String READ_AVERAGE_TIME_TO_WIN = "SELECT AVG(duration) FROM games WHERE winner = ?";
    public static final String READ_USER_RANK = "SELECT COUNT(*) + 1 AS `rank` " +
                    "FROM (SELECT winner, COUNT(*) AS wins " +
                    "FROM games " +
                    "WHERE winner IS NOT NULL " +
                    "GROUP BY winner) AS ranked " +
                    "WHERE ranked.wins > (SELECT COUNT(*) " +
                    "FROM games " +
                    "WHERE winner = ? " +
                    "AND winner IS NOT NULL);";
    public static final String READ_LEADERBOARD = "SELECT " +
                    "DENSE_RANK() OVER (ORDER BY COUNT(g.winner) DESC) as `rank`, " +
                    "u.name, " +
                    "COUNT(g.winner) AS wins, " +
                    "(SELECT COUNT(*) FROM user_to_game ut WHERE ut.user_id = u.user_id) AS games_played, " +
                    "AVG(CASE WHEN g.winner = u.name THEN g.duration ELSE NULL END) AS avg_time_to_win " +
                    "FROM users u " +
                    "LEFT JOIN games g ON u.name = g.winner " +
                    "GROUP BY u.name, u.user_id " +
                    "ORDER BY wins DESC";
    public static final String REFRESH_ONLINE_USERS = "TRUNCATE TABLE online_users";
    public static final String DELETE_ONLINE_USERS = "DELETE FROM online_users WHERE name = ?";
}
