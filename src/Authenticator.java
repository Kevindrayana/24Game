import java.io.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Authenticator extends UnicastRemoteObject implements Auth {
    private DatabaseConnection db_conn;

    public Authenticator() throws RemoteException {
        super();

        // create a DB connection
        try {
            db_conn = new DatabaseConnection();
        } catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        // TODO: Flush the online_users table

        // create OnlineUsers.txt
        try {
            File file = new File("OnlineUsers.txt");
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
        } catch (IOException e) {
            System.err.println("Error creating OnlineUsers.txt: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public LoginStatus login(String username, String password) throws RemoteException {
        // read user from DB
        ArrayList<String> userData = db_conn.read(username, TableName.user_info);
        ArrayList<String> userOnline = db_conn.read(username, TableName.online_users);

        // TODO: this should be atomic
        // check if user exists
        if (userData.isEmpty())
            return LoginStatus.USER_NOT_FOUND;
        // validate user from UserInfo.txt
        if (!password.equals(userData.get(1)))
            return LoginStatus.INVALID_CREDENTIALS;
        // check if user have logged in already
        if (!userOnline.isEmpty())
            return LoginStatus.ALREADY_LOGGED_IN;
        // write user to online_user table
        db_conn.insert(username, password, TableName.online_users);

        System.out.println("Login Successful :)");

        return LoginStatus.SUCCESS;
    }

    @Override
    public RegisterStatus register(String username, String password) throws RemoteException {
        // read user from DB
        ArrayList<String> userData = db_conn.read(username, TableName.user_info);
        // check if username already registered
        if (!userData.isEmpty())
            return RegisterStatus.USERNAME_ALREADY_EXISTED;
        // write user to user_info table
        db_conn.insert(username, password, TableName.user_info);

        System.out.println("Registration Successful ;)");

        if (login(username, password) != LoginStatus.SUCCESS)
            return RegisterStatus.LOGIN_FAIL;

        return RegisterStatus.SUCCESS;
    }

    @Override
    public boolean logout(String username) throws RemoteException {
        try {
            // delete user from online_users
            db_conn.delete(username, TableName.online_users);

            System.out.println("User " + username + " logged out successfully");
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static void main(String[] args) {
        try {
            Authenticator app = new Authenticator();
            System.setSecurityManager(new SecurityManager());
            Naming.rebind("Authenticator", app);
            System.out.println("Service registered!");
        } catch (Exception e) {
            System.err.println("Exception thrown: " + e);
        }
    }

}
