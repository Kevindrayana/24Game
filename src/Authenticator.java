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

        try {
            db_conn = new DatabaseConnection();
            db_conn.refreshOnlineUsers(); // clear online_users
        } catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized LoginStatus login(String username, String password) throws RemoteException {
        try {
            db_conn.beginTransaction();

            // read user from DB
            HashMap<String, String> userData = db_conn.readUsers(username);
            HashMap<String, String> userOnline = db_conn.readOnlineUsers(username);

            // check if user exists and correct password
            if (userData.isEmpty())
                return LoginStatus.USER_NOT_FOUND;
            if (!password.equals(userData.get("password")))
                return LoginStatus.INVALID_CREDENTIALS;
            // check if user have logged in already
            if (!userOnline.isEmpty())
                return LoginStatus.ALREADY_LOGGED_IN;

            db_conn.insertOnlineUsers(username); // write user to online_user table
            db_conn.commit(); // end transaction
            System.out.println("Login Successful :)");
            return LoginStatus.SUCCESS;
        } catch (SQLException e) {
            db_conn.rollback();
            e.printStackTrace();
            return LoginStatus.FAIL;
        }
    }

    @Override
    public synchronized RegisterStatus register(String username, String password) throws RemoteException {
        try {
            db_conn.beginTransaction();
            // read user from DB
            HashMap<String, String> userData = db_conn.readUsers(username);
            // check if username already registered
            if (!userData.isEmpty())
                return RegisterStatus.USERNAME_ALREADY_EXISTED;
            // write user to user_info table
            db_conn.insertUsers(username, password);
            db_conn.commit(); // end transaction

            if (login(username, password) != LoginStatus.SUCCESS)
                return RegisterStatus.LOGIN_FAIL;

            System.out.println("register successful :)");
            return RegisterStatus.SUCCESS;
        } catch (SQLException e) {
            db_conn.rollback();
            e.printStackTrace();
            return RegisterStatus.LOGIN_FAIL;
        }
    }

    @Override
    public synchronized boolean logout(String username) throws RemoteException {
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
