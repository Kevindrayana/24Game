import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Auth extends Remote {
    boolean register(String username, String password, String confirmPassword) throws RemoteException;
    boolean login(String username, String password) throws RemoteException;
    boolean logout(String username) throws RemoteException;
}
