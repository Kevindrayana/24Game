import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.stream.Collectors;

public class JPoker24GameServer implements MessageListener {
    private ArrayList<WaitingPlayer> waitingPlayers = new ArrayList<>();
    private Map<String, Game> activeGames = new HashMap<>();
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private Connection connection;
    private Session session;
    private MessageConsumer receiver;
    private MessageProducer producer;

    public JPoker24GameServer() throws NamingException, JMSException {
        try {
            // Setup RMI
            Authenticator app = new Authenticator();
            Registry registry = LocateRegistry.createRegistry(1099);
            System.setSecurityManager(new SecurityManager());
            Naming.rebind("Authenticator", app);
            System.out.println("Service registered!");

            // Setup JMS
            Context ctx = new InitialContext();
            ConnectionFactory factory = (ConnectionFactory) ctx.lookup("jms/JPoker24GameConnectionFactory");
            Queue queue = (Queue) ctx.lookup("jms/JPoker24GameQueue");
            Topic topic = (Topic) ctx.lookup("jms/JPoker24GameTopic");
            connection = factory.createConnection();

            // Setup receiver (queue) and producer (topic)
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            receiver = session.createConsumer(queue);
            receiver.setMessageListener(this);
            producer = session.createProducer(topic);

            connection.start();

            scheduler.scheduleAtFixedRate(this::checkStartGame, 10, 10, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.err.println("Server error: " + e);
        }
    }

    public static void main(String[] args) {
        try {
            JPoker24GameServer server = new JPoker24GameServer();
            System.out.println("Server running. Press Ctrl+C to stop.");
            Thread.currentThread().join(); // Keep server alive
        } catch (Exception e) {
            System.err.println("Exception thrown: " + e);
        }
    }

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                TextMessage msg = (TextMessage) message;
                String type = msg.getStringProperty("type");
                switch (type) {
                    case "JOIN":
                        handleJoin(msg);
                        break;
                    case "ANSWER":
                        handleAnswer(msg);
                        break;
                }
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private void handleJoin(TextMessage message) throws JMSException {
        String playerName = message.getStringProperty("username");
        long joinTime = message.getLongProperty("timestamp");
        WaitingPlayer player = new WaitingPlayer(playerName, joinTime);
        synchronized (waitingPlayers) {
            waitingPlayers.add(player);
            System.out.println(waitingPlayers.toString());
            long currentTime = System.currentTimeMillis();
            if (waitingPlayers.size() >= 4 || (!waitingPlayers.isEmpty() && waitingPlayers.size() >= 2 &&  (currentTime - waitingPlayers.get(0).getTimestamp() >= 10000))) {
                String[] gamePlayers = waitingPlayers.stream()
                        .limit(4)
                        .map(WaitingPlayer::getPlayername)
                        .toArray(String[]::new);
                startGame(gamePlayers);
                waitingPlayers.subList(0, Math.min(4, waitingPlayers.size())).clear();
            }
        }
    }

    private void handleAnswer(TextMessage message) {
        System.out.println("handling answer...");
    }

    private void checkStartGame() {
        System.out.println("checking...");
        synchronized (waitingPlayers) {
            long currentTime = System.currentTimeMillis();
            if (!waitingPlayers.isEmpty() &&
                    waitingPlayers.size() >= 2 &&
                    (currentTime - waitingPlayers.get(0).getTimestamp() >= 10000)) { // 10 seconds = 10000 ms
                String[] gamePlayers = waitingPlayers.stream()
                        .limit(4)
                        .map(WaitingPlayer::getPlayername)
                        .toArray(String[]::new);
                startGame(gamePlayers);
                waitingPlayers.subList(0, Math.min(4, waitingPlayers.size())).clear();
            }
        }
    }

    private void startGame(String[] players) {
        String gameId = UUID.randomUUID().toString();
        List<Card> cards = new Deck().drawFourCards();
        Game game = new Game(gameId, players, cards);
        activeGames.put(gameId, game);

        // Notify all subscribers that the game has started
        try {
            TextMessage msg = session.createTextMessage();
            msg.setStringProperty("type", "GAME_START");
            msg.setStringProperty("gameId", gameId);
            msg.setStringProperty("players", String.join(",", players)); // Send player list as message text
            msg.setStringProperty("cards", cards.stream()
                    .map(Card::toString)
                    .collect(Collectors.joining(",")));
            producer.send(msg);
            System.out.println("Sent GAME_START to topic for game " + gameId + " with players: " + msg.getText());
        } catch (JMSException e) {
            System.err.println("Error sending GAME_START message: " + e);
        }

        game.start();
    }

    private static class Game {
        private String gameId;
        private List<String> players;
        private List<Card> cards;

        Game(String gameId, String[] players, List<Card> cards) {
            this.gameId = gameId;
            this.players = new ArrayList<>(Arrays.asList(players)); // Convert array to list
            this.cards = new ArrayList<>(cards);
        }

        void start() {
            System.out.println("Game " + gameId + " started with players: " + players + " and cards: " + cards);
        }
    }

    private class WaitingPlayer {
        String playername;
        long timestamp;

        WaitingPlayer(String playername, long timestamp) {
            this.playername = playername;
            this.timestamp = timestamp;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String getPlayername() {
            return playername;
        }

        @Override
        public String toString() {
            return playername + " (" + timestamp + ")";
        }
    }
}