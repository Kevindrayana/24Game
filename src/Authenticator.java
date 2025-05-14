import java.io.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class Authenticator extends UnicastRemoteObject implements Auth {
    public Authenticator() throws RemoteException {
        super();

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
        try {
            HashMap<String, String> usernameToPassword = getUsersInfoFromFile("UserInfo.txt");
            HashMap<String, String> onlineUsers = getUsersInfoFromFile("OnlineUsers.txt");

            // check if user exists
            if (!usernameToPassword.containsKey(username))
                return LoginStatus.USER_NOT_FOUND;
            // validate user from UserInfo.txt
            if (!password.equals(usernameToPassword.get(username)))
                return LoginStatus.INVALID_CREDENTIALS;
            // check if user have logged in already
            if (onlineUsers.containsKey(username))
                return LoginStatus.ALREADY_LOGGED_IN;

            // add user to OnlineUsers.txt
            BufferedWriter writer = new BufferedWriter(new FileWriter("OnlineUsers.txt", true));
            writer.write(String.format("%s,%s%n", username, password));
            writer.flush();
            writer.close();

            System.out.println("Login Successful :)");

            return LoginStatus.SUCCESS;
        } catch (IOException e) {
            e.printStackTrace();
            return LoginStatus.FAIL;
        }
    }

    @Override
    public RegisterStatus register(String username, String password) throws RemoteException {
        try {
            HashMap<String, String> usernameToPassword = getUsersInfoFromFile("UserInfo.txt");

            // check if username already registered
            if (usernameToPassword.get(username) != null)
                return RegisterStatus.USERNAME_ALREADY_EXISTED;

            // add user to UserInfo.txt
            BufferedWriter writer = new BufferedWriter(new FileWriter("UserInfo.txt", true));
            writer.write(String.format("%s,%s%n", username, password));
            writer.flush();
            writer.close();

            System.out.println("Registration Successful ;)");

            if (login(username, password) != LoginStatus.SUCCESS) {
                return RegisterStatus.LOGIN_FAIL;
            }

            return RegisterStatus.SUCCESS;
        } catch (IOException e) {
            e.printStackTrace();
            return RegisterStatus.FAIL;
        }
    }

    public boolean logout(String username) throws RemoteException {
        try {
            // read current online users
            HashMap<String, String> onlineUsers = getUsersInfoFromFile("OnlineUsers.txt");
            // remove the user
            onlineUsers.remove(username);
            // rewrite the file with remaining users
            BufferedWriter writer = new BufferedWriter(new FileWriter("OnlineUsers.txt"));
            for (Map.Entry<String, String> entry : onlineUsers.entrySet()) {
                writer.write(String.format("%s,%s%n", entry.getKey(), entry.getValue()));
            }
            writer.flush();
            writer.close();

            System.out.println("User " + username + " logged out successfully");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private HashMap<String, String> getUsersInfoFromFile(String fileName) {
        HashMap<String, String> res = new HashMap<>();
        File file = new File(fileName);
        try {
            // create file if it doesn't exist
            if (!file.exists()) {
                file.createNewFile();
                return res;
            }

            FileReader fileReader = new FileReader(file);
            BufferedReader reader = new BufferedReader(fileReader);

            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] arr = line.split(",");
                String username_ = arr[0];
                String password_ = arr[1];
                res.put(username_, password_);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
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
