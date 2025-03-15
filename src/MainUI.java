import javax.naming.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.jms.*;

public class MainUI implements MessageListener {
    private JFrame frame;
    private String username;
    private Auth authenticator;
    private Queue queue;
    private Topic topic;
    private Connection connection;
    private Session session;
    private MessageProducer sender;
    private MessageConsumer consumer;
    private JPanel gamePanel;

    public MainUI(String username) throws NamingException, JMSException {
        try {
            this.username = username;
            // Setup RMI
            authenticator = (Auth) Naming.lookup("Authenticator");

            // Setup JMS
            Context ctx = new InitialContext();
            ConnectionFactory factory = (ConnectionFactory) ctx.lookup("jms/JPoker24GameConnectionFactory");
            queue = (Queue) ctx.lookup("jms/JPoker24GameQueue");
            topic = (Topic) ctx.lookup("jms/JPoker24GameTopic");
            connection = factory.createConnection();

            // setup sender (queue) and consumer (topic)
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            sender = session.createProducer(queue);
            consumer = session.createConsumer(topic); // Subscribe to topic
            consumer.setMessageListener(this);

            connection.start();
        } catch (Exception e) {
            System.err.println("Failed accessing RMI or JMS: " + e);
        }
    }

    public void paint() {
        frame = new JFrame("JPoker 24-Game");
        frame.setPreferredSize(new Dimension(800, 600));

        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel userProfilePanel = createUserProfilePanel();
        tabbedPane.addTab("User Profile", userProfilePanel);

        gamePanel = createPlayGamePanel(); // Store reference to update later
        tabbedPane.addTab("Play Game", gamePanel);
        tabbedPane.addTab("Leader Board", new JPanel());

        JPanel logoutPanel = createLogoutPanel();
        tabbedPane.addTab("Logout", logoutPanel);

        frame.add(tabbedPane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                cleanup(); // Clean up JMS resources on close
            }
        });
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel createUserProfilePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Username display
        JLabel welcomeLabel = new JLabel("Welcome, " + username);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("wins", 0);
        userInfo.put("gamesPlayed", 0);
        userInfo.put("averageTimeToWin", 0.0);
        userInfo.put("rank", 0);

        JLabel winsLabel = new JLabel("Wins: " + userInfo.get("wins"));
        winsLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        winsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel gamesPlayedLabel = new JLabel("Games Played: " + userInfo.get("gamesPlayed"));
        gamesPlayedLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        gamesPlayedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        Object avgTimeObj = userInfo.get("averageTimeToWin");
        double avgTime = (avgTimeObj instanceof Number) ? ((Number) avgTimeObj).doubleValue() : 0.0;
        JLabel avgTimeLabel = new JLabel("Average Time to Win: " + String.format("%.2f", avgTime) + " sec");
        avgTimeLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        avgTimeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel rankLabel = new JLabel("Rank: " + userInfo.get("rank"));
        rankLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        rankLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(Box.createVerticalGlue());
        panel.add(welcomeLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(winsLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(gamesPlayedLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(avgTimeLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(rankLabel);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel createLogoutPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel confirmLabel = new JLabel("Are you sure you want to logout?");
        confirmLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton logoutButton = new JButton("Confirm Logout");
        logoutButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutButton.addActionListener(new LogoutButtonListener());

        panel.add(Box.createVerticalGlue());
        panel.add(confirmLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(logoutButton);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel createPlayGamePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel instructionLabel = new JLabel("Click 'New Game' to join a game session.");
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton newGameButton = new JButton("New Game");
        newGameButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        newGameButton.addActionListener(new PlayGameListener());

        panel.add(Box.createVerticalGlue());
        panel.add(instructionLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(newGameButton);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    class LogoutButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                if (authenticator.logout(username)) {
                    frame.dispose();
                    cleanup(); // Clean up JMS resources
                    new LoginUI().paint();
                } else {
                    frame.dispose();
                    cleanup(); // Clean up JMS resources
                    new ErrorUI("Logout failed", username).paint();
                }
            } catch (RemoteException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    class PlayGameListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                TextMessage message = session.createTextMessage();
                message.setStringProperty("type", "JOIN");
                message.setStringProperty("username", username);
                message.setLongProperty("timestamp", System.currentTimeMillis());
                sender.send(message);
                System.out.println("Joining game as: " + username);
            } catch (JMSException ex) {
                System.err.println("Failed to send JOIN message: " + ex);
                JOptionPane.showMessageDialog(frame, "Failed to join game: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                TextMessage msg = (TextMessage) message;
                String type = msg.getStringProperty("type");
                String playersList = message.getStringProperty("players"); // Comma-separated players

                if (type != null && playersList.contains(username)) {
                    switch (type) {
                        case "GAME_START":
                            handleGameStart(msg);
                            break;
                    }
                }
            }
        } catch (JMSException e) {
            System.err.println("Error processing message: " + e);
        }
    }

    void handleGameStart(TextMessage message) throws JMSException {
        String gameId = message.getStringProperty("gameId");
        String playersList = message.getStringProperty("players"); // Comma-separated players
        String cardsString = message.getStringProperty("cards"); // Comma-separated cards

        System.out.println("Game " + gameId + " started with players: " + playersList + " and cards: " + cardsString);

        // Update the Play Game tab with game details
        gamePanel.removeAll(); // Clear existing content
        gamePanel.setLayout(new BoxLayout(gamePanel, BoxLayout.Y_AXIS));

        JLabel gameLabel = new JLabel("Game Started (ID: " + gameId + ")");
        gameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel playersLabel = new JLabel("Players: " + playersList);
        playersLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel cardsLabel = new JLabel("Cards: " + cardsString);
        cardsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        gamePanel.add(Box.createVerticalGlue());
        gamePanel.add(gameLabel);
        gamePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        gamePanel.add(playersLabel);
        gamePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        gamePanel.add(cardsLabel);
        gamePanel.add(Box.createVerticalGlue());

        gamePanel.revalidate();
        gamePanel.repaint();

        // Switch to the "Play Game" tab
        JTabbedPane tabbedPane = (JTabbedPane) frame.getContentPane().getComponent(0);
        tabbedPane.setSelectedIndex(1);
    }

    private void cleanup() {
        try {
            if (sender != null) sender.close();
            if (consumer != null) consumer.close();
            if (session != null) session.close();
            if (connection != null) connection.close();
        } catch (JMSException e) {
            System.err.println("Error closing JMS resources: " + e);
        }
    }
}