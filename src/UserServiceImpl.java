import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserServiceImpl extends UnicastRemoteObject implements UserService {
    private DatabaseConnection db_conn;

    public UserServiceImpl() throws RemoteException, SQLException, InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        super();
        db_conn = new DatabaseConnection();
    }

    @Override
    public Map<String, Object> getUserInfo(String username) throws RemoteException {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("wins", db_conn.readUserWins(username));
        userInfo.put("gamesPlayed", db_conn.readGamesPlayed(username));
        userInfo.put("averageTimeToWin", db_conn.readAvgTimeToWin(username));
        userInfo.put("rank", db_conn.readUserRank(username));
        System.out.println(userInfo);
        return userInfo;
    }

    @Override
    public List<LeaderboardEntry> getLeaderboard() throws RemoteException {
        return db_conn.readLeaderboard();
    }

    @Override
    public void updateLeaderboard(String gameId, String winner, int duration, List<String> players)
            throws RemoteException {
        try {
            db_conn.beginTransaction();

            db_conn.insertGames(gameId, winner, duration);
            for (String player : players) {
                db_conn.insertUserToGame(player, gameId);
            }
            db_conn.commit();

        } catch (SQLException e) {
            db_conn.rollback();
            e.printStackTrace();
        }
    }

}