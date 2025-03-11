import java.io.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class Authenticator extends UnicastRemoteObject implements Auth {
    private DatabaseConnection db_conn;

    public Authenticator() throws RemoteException {
        super();

        // create a DB connection
        try {
            db_conn = new DatabaseConnection();
            db_conn.refreshOnlineUsers(); // clear online_users
        } catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public LoginStatus login(String username, String password) throws RemoteException {
        // read user from DB
        HashMap<String, String> userData = db_conn.readUsers(username);
        HashMap<String, String> userOnline = db_conn.readOnlineUsers(username);

        // TODO: this should be atomic
        // check if user exists
        if (userData.isEmpty())
            return LoginStatus.USER_NOT_FOUND;
        // validate user from UserInfo.txt
        if (!password.equals(userData.get("password"))) return LoginStatus.INVALID_CREDENTIALS;
        // check if user have logged in already
        if (!userOnline.isEmpty())
            return LoginStatus.ALREADY_LOGGED_IN;
        // write user to online_user table
        db_conn.insertOnlineUsers(username);

        System.out.println("Login Successful :)");

        return LoginStatus.SUCCESS;
    }

    @Override
    public RegisterStatus register(String username, String password) throws RemoteException {
        // read user from DB
        HashMap<String, String> userData = db_conn.readUsers(username);
        // check if username already registered
        if (!userData.isEmpty())
            return RegisterStatus.USERNAME_ALREADY_EXISTED;
        // write user to user_info table
        db_conn.insertUsers(username, password);
        System.out.println("Registration Successful ;)");

        if (login(username, password) != LoginStatus.SUCCESS)
            return RegisterStatus.LOGIN_FAIL;

        return RegisterStatus.SUCCESS;
    }

    @Override
    public boolean logout(String username) throws RemoteException {
        try {
            // delete user from online_users
            db_conn.deleteOnlineUsers(username);
            System.out.println("User " + username + " logged out successfully");
            return true;
        } catch (SQLException e) {
            System.err.println(e);
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
