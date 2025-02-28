import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Auth extends Remote {
    RegisterStatus register(String username, String password) throws RemoteException;
    LoginStatus login(String username, String password) throws RemoteException;
    boolean logout(String username) throws RemoteException;
}
