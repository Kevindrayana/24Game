import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface UserService extends Remote {
    Map<String, Object> getUserInfo(String username) throws RemoteException;

    List<LeaderboardEntry> getLeaderboard() throws RemoteException;

    void updateLeaderboard(String gameId, String winner, int duration, List<String> players) throws RemoteException;

}