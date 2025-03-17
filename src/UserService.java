import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface UserService extends Remote {
    Map<String, Object> getUserInfo(String username) throws RemoteException;
    List<LeaderboardEntry> getLeaderboard() throws RemoteException;
    void insertGame(String gameId, String winner, int duration) throws RemoteException;
    void insertUserToGame(String user, String gameId) throws RemoteException;
}