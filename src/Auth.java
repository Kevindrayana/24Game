import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Auth extends Remote {
    void register(String username, String password) throws RemoteException;
    void login(String username, String password) throws RemoteException;

}
