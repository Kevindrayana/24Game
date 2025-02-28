import java.sql.*;
import java.util.ArrayList;

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

    public void insert(String name, String password, TableName tableName) {
        try {
            PreparedStatement stmt = conn.prepareStatement(String.format("INSERT INTO %s (name, password) VALUES (?, ?)", tableName.name()));
            stmt.setString(1, name);
            stmt.setString(2, password);
            stmt.execute();
            System.out.println(name + "inserted into user_info");
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Error inserting record: "+e);
        }
    }

    public ArrayList<String> read(String name, TableName tableName) {
        ArrayList<String> result = new ArrayList<>();
        try {
            PreparedStatement stmt = conn.prepareStatement(String.format("SELECT * FROM %s WHERE NAME = ?", tableName.name()));
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                return result; // Return empty list if no record found
            }

            result.add(rs.getString("name"));    // Column names must match DB
            result.add(rs.getString("password"));
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

    public void delete (String name, TableName tableName) throws SQLException, IllegalArgumentException{
        PreparedStatement stmt = conn.prepareStatement(String.format("DELETE FROM %s WHERE name = ?", tableName));
        stmt.setString(1, name);
        int rows = stmt.executeUpdate();
        if (rows > 0) {
            System.out.println("Record of " + name + " removed");
        } else {
            System.out.println(name + " not found!");
            throw new IllegalArgumentException(name + " not found!");
        }
    }
}
