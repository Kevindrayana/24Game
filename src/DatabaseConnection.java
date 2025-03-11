import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class DatabaseConnection {
    private static final String DB_HOST = "localhost";
    private static final String DB_USER = "game24";
    private static final String DB_PASS = "game24PASS";
    private static final String DB_NAME = "game24";
    private Connection conn;
    public DatabaseConnection() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver").newInstance();

        conn = DriverManager.getConnection("jdbc:mysql://"+DB_HOST+"/"+DB_NAME+"?user="+DB_USER+"&password="+DB_PASS);
        System.out.println("Connected to database successfully!");
    }

    public void insertUsers(String name, String password) {
        try {
            PreparedStatement stmt = conn.prepareStatement(SqlStatements.INSERT_USERS);
            stmt.setString(1, name);
            stmt.setString(2, password);
            stmt.execute();
            System.out.println(name + "inserted into users");
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Error inserting record: "+e);
        }
    }
    public void insertOnlineUsers(String name) {
        try {
            PreparedStatement stmt = conn.prepareStatement(SqlStatements.INSERT_ONLINE_USERS);
            stmt.setString(1, name);
            stmt.execute();
            System.out.println(name + "inserted into online_users");
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Error inserting record: "+e);
        }
    }
    public void insertGames(String winner, int duration) {
        try {
            PreparedStatement stmt = conn.prepareStatement(SqlStatements.INSERT_GAMES);
            stmt.setString(1, winner);
            stmt.setInt(2, duration);
            stmt.execute();
            System.out.println("inserted into games");
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Error inserting record: "+e);
        }
    }
    public void insertUserToGame(int userID, int gameID) {
        try {
            PreparedStatement stmt = conn.prepareStatement(SqlStatements.INSERT_USER_TO_GAME);
            stmt.setInt(1, userID);
            stmt.setInt(2, gameID);
            stmt.execute();
            System.out.println("inserted into user_to_game");
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Error inserting record: "+e);
        }
    }

    public HashMap<String, String> readUsers(String username) {
        HashMap<String, String> result = new HashMap<>();
        try {
            PreparedStatement stmt = conn.prepareStatement(SqlStatements.READ_USERS);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                return result; // Return empty if no record found
            }

            result.put("name", rs.getString("name"));
            result.put("password", rs.getString("password"));
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Error reading record: "+e);
        }
        return result;
    }

    public HashMap<String, String> readOnlineUsers(String username) {
        HashMap<String, String> result = new HashMap<>();
        try {
            PreparedStatement stmt = conn.prepareStatement(SqlStatements.READ_ONLINE_USERS);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                return result; // Return empty if no record found
            }

            result.put("name", rs.getString("name"));
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Error reading record: "+e);
        }
        return result;
    }
    public void update(String name, String newPassword) throws SQLException {
        try {
            conn.setAutoCommit(false);  // Start transaction

            PreparedStatement stmt1 = conn.prepareStatement("UPDATE user_info SET password = ? WHERE name = ?");
            stmt1.setString(1, newPassword);
            stmt1.setString(2, name);
            int rowsAffected1 = stmt1.executeUpdate();

            PreparedStatement stmt2 = conn.prepareStatement("UPDATE online_users SET password = ? WHERE name = ?");
            stmt2.setString(1, newPassword);
            stmt2.setString(2, name);
            int rowsAffected2 = stmt2.executeUpdate();

            if (rowsAffected1 == 0 || rowsAffected2 == 0) {
                conn.rollback();
                System.out.println(name + " not found");
            }
            conn.commit();
            System.out.println("Password of " + name + " updated");
        } catch (SQLException e) {
            conn.rollback();  // Rollback if error occurs
            throw e;
        } finally {
            conn.setAutoCommit(true);  // Reset to default
        }
    }

    public void deleteOnlineUsers(String username) throws SQLException, IllegalArgumentException{
        PreparedStatement stmt = conn.prepareStatement(SqlStatements.DELETE_ONLINE_USERS);
        stmt.setString(1, username);
        int rows = stmt.executeUpdate();
        if (rows > 0) {
            System.out.println("Record of " + username + " removed");
        } else {
            System.out.println(username + " not found!");
            throw new IllegalArgumentException(username + " not found!");
        }
    }

    public void refreshOnlineUsers() {
        try {
            PreparedStatement stmt = conn.prepareStatement(SqlStatements.REFRESH_ONLINE_USERS);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to refresh online_users table "+e);
        }
    }
}
