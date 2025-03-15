public class SqlStatements {
    public static final String INSERT_USERS = "INSERT INTO users (name, password) VALUES (?, ?)";
    public static final String INSERT_ONLINE_USERS = "INSERT INTO online_users (name) VALUES (?)";
    public static final String INSERT_GAMES = "INSERT INTO games (winner, duration) VALUES (?, ?)";
    public static final String INSERT_USER_TO_GAME = "INSERT INTO user_to_game (user_id, game_id) VALUES (?, ?)";
    public static final String READ_USERS = "SELECT name, password FROM users WHERE name = ?";
    public static final String READ_ONLINE_USERS = "SELECT name FROM online_users WHERE name = ?";
    // public static final String READ_GAMES = "SELECT name FROM online_users WHERE name = ?";
    // public static final String READ_USER_TO_GAME = "SELECT name FROM online_users WHERE name = ?";

    // public static final String UPDATE_USERS = "";
    public static final String REFRESH_ONLINE_USERS = "TRUNCATE TABLE online_users";

    public static final String DELETE_USERS = "";
    public static final String DELETE_ONLINE_USERS = "DELETE FROM online_users WHERE name = ?";
}
