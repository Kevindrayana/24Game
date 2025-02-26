import java.io.*;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

public class Authenticator extends UnicastRemoteObject implements Auth{
    public Authenticator() throws RemoteException{
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
    public void login(String username, String password) throws RemoteException{
        try {
            HashMap<String, String> usernameToPassword = getUsersInfoFromFile("UserInfo.txt");

            // Check if user exists
            if (!usernameToPassword.containsKey(username)) {
                System.out.println("User not found");
                return;
            }

            // validate user from UserInfo.txt
            if (!password.equals(usernameToPassword.get(username))) {
                System.out.println("Invalid credentials");
                return;
            }

            // check if user have logged in already
            HashMap<String, String> onlineUsers = getUsersInfoFromFile("OnlineUsers.txt");
            if (onlineUsers.containsKey(username)) {
                System.out.println("User already logged in");
                return;
            }

            // add user to OnlineUsers.txt
            BufferedWriter writer = new BufferedWriter(new FileWriter("OnlineUsers.txt", true));
            writer.write(String.format("%s,%s%n", username, password));
            writer.flush();
            writer.close();

            System.out.println("Login Successful :)");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Login failed", e);
        }
    }

    @Override
    public void register(String username, String password) throws RemoteException {
        try {
            // read the UserInfo.txt file to get all registered users
            HashMap<String, String> usernameToPassword = getUsersInfoFromFile("UserInfo.txt");

            // check if username already registered
            if (usernameToPassword.get(username) != null) {
                System.out.println("Username already exists");
                return;
            }
            // add user to UserInfo.txt
            BufferedWriter writer = new BufferedWriter(new FileWriter("UserInfo.txt", true));
            writer.write(String.format("%s,%s%n", username, password));
            writer.flush();
            writer.close();

            login(username, password);

            System.out.println("Registration Successful ;)");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // TODO: might need to change the return type later
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
        } catch (Exception e){
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
