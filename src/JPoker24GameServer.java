import javax.naming.*;
import java.rmi.*;
import javax.jms.*;
import java.util.*;
import java.io.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class JPoker24GameServer extends JMSHandler implements MessageListener {
    // List to hold players waiting for the game to start
    private List<Player> waitingPlayers = new ArrayList<>();
    // Map to store active games
    private Map<String, GameInstance> activeGames = new HashMap<>();
    // Scheduler to check conditions periodically
//    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    // Time when the first player joined (in milliseconds)
    private long firstJoinTime = -1;
    public JPoker24GameServer() throws NamingException, JMSException {
        super("localhost");
        try {
            Authenticator app = new Authenticator();
            Registry registry = LocateRegistry.createRegistry(1099);
            System.setSecurityManager(new SecurityManager());
            Naming.rebind("Authenticator", app);
            System.out.println("Service registered!");
            queueReceiver.setMessageListener(this);  // Set the listener
//            scheduler.scheduleAtFixedRate(this::checkStartGame, 1, 1, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.err.println("Server error: " + e);
        }

    }
    public static void main(String[] args) {
        try {
            JPoker24GameServer server = new JPoker24GameServer();
        } catch (Exception e) {
            System.err.println("Exception thrown: " + e);
        }
    }
    private static class Player {
        String id;
        Player(String id) {
            this.id = id;
        }
    }
    private static class GameInstance {
        String gameId;
        List<Player> players;

        GameInstance(String gameId, List<Player> players) {
            this.gameId = gameId;
            this.players = players;
        }
    }

    @Override
    public void onMessage(Message message) {
        try {
            // Process received message
            if (message instanceof TextMessage) {
                String type = message.getStringProperty("type");
//                System.out.println("getting msg..." + message.getText());
                switch (type) {
                    case "JOIN":
                        handleGameStart(message);
                        break;
                    case "ANSWER":
                        handleAnswer(message);
                        break;
                    // Add other message types as needed
                }
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private void handleGameStart(Message message) {
        System.out.println("Starting game...");
    }
    private void handleAnswer(Message message) {
        System.out.println("handling answer...");
    }
}
