import javax.naming.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.jms.Connection;
import java.io.File;
import javax.jms.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MainUI implements MessageListener {
    private JFrame frame;
    private String username;
    private Auth authenticator;
    private UserService userService;
    private Queue queue;
    private Topic topic;
    private Connection connection;
    private Session session;
    private MessageProducer sender;
    private MessageConsumer consumer;
    private JPanel gamePanel;
    private String gameId;
    private JTextField expressionField;

    public MainUI(String username) throws NamingException, JMSException {
        try {
            // setup RMI
            this.username = username;
            authenticator = (Auth) Naming.lookup("rmi://localhost:1099/Authenticator");
            userService = (UserService) Naming.lookup("rmi://localhost:1099/UserService");

            // setup JMS
            Context ctx = new InitialContext();
            ConnectionFactory factory = (ConnectionFactory) ctx.lookup("jms/JPoker24GameConnectionFactory");
            queue = (Queue) ctx.lookup("jms/JPoker24GameQueue");
            topic = (Topic) ctx.lookup("jms/JPoker24GameTopic");
            connection = factory.createConnection();

            // setup sender (queue) and consumer (topic)
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            sender = session.createProducer(queue);
            consumer = session.createConsumer(topic);
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

        // initial panels
        JPanel userProfilePanel = createUserProfilePanel();
        gamePanel = createPlayGamePanel();
        JPanel leaderboardPanel = createLeaderboardPanel();
        JPanel logoutPanel = createLogoutPanel();

        tabbedPane.addTab("User Profile", userProfilePanel);
        tabbedPane.addTab("Play Game", gamePanel);
        tabbedPane.addTab("Leader Board", leaderboardPanel);
        tabbedPane.addTab("Logout", logoutPanel);

        // refresh panels on tab selection
        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int idx = tabbedPane.getSelectedIndex();
                String title = tabbedPane.getTitleAt(idx);
                switch (title) {
                    case "User Profile":
                        tabbedPane.setComponentAt(idx, createUserProfilePanel());
                        break;
                    case "Leader Board":
                        tabbedPane.setComponentAt(idx, createLeaderboardPanel());
                        break;
                    default:
                        // no action for other tabs
                }
                frame.revalidate();
                frame.repaint();
            }
        });

        frame.add(tabbedPane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                // logout before exit
                try {
                    if (authenticator.logout(username)) {
                        System.out.println("User " + username + " logged out successfully.");
                    } else {
                        System.err.println("Logout failed for user " + username);
                    }
                } catch (RemoteException ex) {
                    System.err.println("Error during logout: " + ex);
                }
                // cleanup JMS resources
                cleanup();
                // dispose UI
                frame.dispose();
                System.exit(0);
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

        JLabel welcomeLabel = new JLabel("Welcome, " + username);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        try {
            Map<String, Object> userInfo = userService.getUserInfo(username);

            JLabel winsLabel = new JLabel("Wins: " + userInfo.get("wins"));
            winsLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            winsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel gamesPlayedLabel = new JLabel("Games Played: " + userInfo.get("gamesPlayed"));
            gamesPlayedLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            gamesPlayedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Safely convert averageTimeToWin to Double
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
        } catch (RemoteException e) {
            System.err.println("Failed to fetch user info: " + e);
            JLabel errorLabel = new JLabel("Unable to load user stats");
            errorLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(errorLabel);
        }

        return panel;
    }

    private JPanel createLeaderboardPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel panel = new JPanel(new BorderLayout());

        try {
            List<LeaderboardEntry> leaderboard = userService.getLeaderboard();

            String[] columnNames = { "Rank", "Username", "Wins", "Games Played", "Avg Time to Win (sec)" };
            Object[][] data = new Object[leaderboard.size()][5];
            for (int i = 0; i < leaderboard.size(); i++) {
                LeaderboardEntry entry = leaderboard.get(i);
                data[i][0] = i + 1; // Rank starts at 1
                data[i][1] = entry.getUsername();
                data[i][2] = entry.getWins();
                data[i][3] = entry.getGamesPlayed();
                data[i][4] = String.format("%.2f", entry.getAverageTimeToWin());
            }

            JTable table = new JTable(data, columnNames);
            table.setFillsViewportHeight(true);
            table.setFont(new Font("Arial", Font.PLAIN, 14));
            table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
            JScrollPane scrollPane = new JScrollPane(table);

            panel.add(scrollPane, BorderLayout.CENTER);
        } catch (RemoteException e) {
            System.err.println("Failed to fetch leaderboard: " + e);
            JLabel errorLabel = new JLabel("Unable to load leaderboard");
            errorLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(errorLabel, BorderLayout.CENTER);
        }

        mainPanel.add(panel, BorderLayout.CENTER);
        return mainPanel;
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

    class LogoutButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                if (authenticator.logout(username)) {
                    frame.dispose();
                    new LoginUI().paint();
                } else {
                    frame.dispose();
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

                gamePanel.removeAll();
                gamePanel.setLayout(new BoxLayout(gamePanel, BoxLayout.Y_AXIS));
                JLabel waitingLabel = new JLabel("Waiting for players...");
                waitingLabel.setFont(new Font("Arial", Font.PLAIN, 18));
                waitingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                gamePanel.add(Box.createVerticalGlue());
                gamePanel.add(waitingLabel);
                gamePanel.add(Box.createVerticalGlue());
                gamePanel.revalidate();
                gamePanel.repaint();

                JTabbedPane tabbedPane = (JTabbedPane) frame.getContentPane().getComponent(0);
                tabbedPane.setSelectedIndex(1);
            } catch (JMSException ex) {
                System.err.println("Failed to send JOIN message: " + ex);
                JOptionPane.showMessageDialog(frame, "Failed to join game: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    class SubmitListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                TextMessage message = session.createTextMessage();
                message.setStringProperty("type", "ANSWER");
                message.setStringProperty("gameId", gameId);
                message.setStringProperty("username", username);
                message.setStringProperty("answer", expressionField.getText());

                sender.send(message);
                System.out.println("Sending answer: " + expressionField.getText());
            } catch (JMSException ex) {
                System.err.println("Failed to send ANSWER message: " + ex);
                JOptionPane.showMessageDialog(frame, "Failed to submit answer: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                TextMessage msg = (TextMessage) message;
                String type = msg.getStringProperty("type");
                String playersList = msg.getStringProperty("players");

                if (type != null && playersList != null && playersList.contains(username)) {
                    switch (type) {
                        case "GAME_START":
                            handleGameStart(msg);
                            break;
                        case "GAME_OVER":
                            handleGameOver(msg);
                            break;
                    }
                }
            }
        } catch (JMSException e) {
            System.err.println("Error processing message: " + e);
        }
    }

    void handleGameStart(TextMessage message) throws JMSException {
        gameId = message.getStringProperty("gameId");
        String playersList = message.getStringProperty("players");
        String cardsString = message.getStringProperty("cards");

        gamePanel.removeAll();
        gamePanel.setLayout(new BorderLayout());

        JPanel cardsPanel = new JPanel();
        cardsPanel.setLayout(new FlowLayout());
        String[] cards = cardsString.split(",");
        System.out.println("Loading cards: " + Arrays.toString(cards));

        for (String card : cards) {
            String cardLower = card.toLowerCase();
            File imageFile = new File("images/cards/" + cardLower + ".gif");

            if (imageFile.exists()) {
                ImageIcon cardIcon = new ImageIcon(imageFile.getAbsolutePath());
                if (cardIcon.getImageLoadStatus() == MediaTracker.COMPLETE) {
                    JLabel cardLabel = new JLabel(cardIcon);
                    cardsPanel.add(cardLabel);
                } else {
                    System.err.println("Failed to load image: " + cardLower);
                }
            } else {
                System.err.println("Image file not found: " + cardLower);
            }
        }
        gamePanel.add(cardsPanel, BorderLayout.CENTER);

        JPanel playersPanel = new JPanel();
        playersPanel.setLayout(new BoxLayout(playersPanel, BoxLayout.Y_AXIS));
        playersPanel.setPreferredSize(new Dimension(200, 0));
        playersPanel.setBorder(BorderFactory.createTitledBorder("Players"));
        String[] players = playersList.split(",");
        for (String player : players) {
            JLabel playerLabel = new JLabel(player);
            playersPanel.add(playerLabel);
        }
        gamePanel.add(playersPanel, BorderLayout.EAST);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));

        JLabel instructionLabel = new JLabel("Enter your expression to make 24:");
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        expressionField = new JTextField(20);
        expressionField.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(new SubmitListener());
        JButton noAnswerButton = new JButton("No Answer");
        buttonPanel.add(submitButton);
        buttonPanel.add(noAnswerButton);

        inputPanel.add(instructionLabel);
        inputPanel.add(expressionField);
        inputPanel.add(buttonPanel);

        gamePanel.add(inputPanel, BorderLayout.SOUTH);

        JLabel gameLabel = new JLabel("Game ID: " + gameId);
        gameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gamePanel.add(gameLabel, BorderLayout.NORTH);

        gamePanel.revalidate();
        gamePanel.repaint();

        JTabbedPane tabbedPane = (JTabbedPane) frame.getContentPane().getComponent(0);
        tabbedPane.setSelectedIndex(1);
    }

    void handleGameOver(TextMessage message) throws JMSException {
        gameId = message.getStringProperty("gameId");
        String winner = message.getStringProperty("winner");
        String answer = message.getStringProperty("answer");
        int duration = message.getIntProperty("duration");

        System.out.println("Game " + gameId + " over. Winner: " + winner + ", Answer: " + answer);

        gamePanel.removeAll();
        gamePanel.setLayout(new BoxLayout(gamePanel, BoxLayout.Y_AXIS));

        JLabel winnerLabel = new JLabel("Winner of game " + gameId + " is " + winner + "!");
        winnerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        winnerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel answerLabel = new JLabel("Winning Expression: " + answer);
        answerLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        answerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel durationLabel = new JLabel("Finished in " + duration + " seconds");
        durationLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        durationLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        gamePanel.add(Box.createVerticalGlue());
        gamePanel.add(winnerLabel);
        gamePanel.add(Box.createRigidArea(new Dimension(0, 20)));
        gamePanel.add(answerLabel);
        gamePanel.add(Box.createRigidArea(new Dimension(0, 20)));
        gamePanel.add(durationLabel);
        gamePanel.add(Box.createVerticalGlue());

        JButton newGameButton = new JButton("New Game");
        newGameButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        newGameButton.addActionListener(new PlayGameListener());
        gamePanel.add(newGameButton);

        gamePanel.revalidate();
        gamePanel.repaint();

        JTabbedPane tabbedPane = (JTabbedPane) frame.getContentPane().getComponent(0);
        tabbedPane.setSelectedIndex(1);
    }

    private void cleanup() {
        try {
            if (sender != null)
                sender.close();
            if (consumer != null)
                consumer.close();
            if (session != null)
                session.close();
            if (connection != null)
                connection.close();
        } catch (JMSException e) {
            System.err.println("Error closing JMS resources: " + e);
        }
    }
}