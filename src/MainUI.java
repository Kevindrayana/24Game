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
import java.io.File;
import javax.jms.*;
import java.util.Arrays;

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
    private String gameId;
    private JTextField expressionField;

    public MainUI(String username) throws NamingException, JMSException {
        try {
            this.username = username;
            authenticator = (Auth) Naming.lookup("Authenticator");

            Context ctx = new InitialContext();
            ConnectionFactory factory = (ConnectionFactory) ctx.lookup("jms/JPoker24GameConnectionFactory");
            queue = (Queue) ctx.lookup("jms/JPoker24GameQueue");
            topic = (Topic) ctx.lookup("jms/JPoker24GameTopic");
            connection = factory.createConnection();

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

        JPanel userProfilePanel = createUserProfilePanel();
        tabbedPane.addTab("User Profile", userProfilePanel);

        gamePanel = createPlayGamePanel();
        tabbedPane.addTab("Play Game", gamePanel);
        tabbedPane.addTab("Leader Board", new JPanel());

        JPanel logoutPanel = createLogoutPanel();
        tabbedPane.addTab("Logout", logoutPanel);

        frame.add(tabbedPane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                cleanup();
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
                    cleanup();
                    new LoginUI().paint();
                } else {
                    frame.dispose();
                    cleanup();
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
                // Send JOIN message
                TextMessage message = session.createTextMessage();
                message.setStringProperty("type", "JOIN");
                message.setStringProperty("username", username);
                message.setLongProperty("timestamp", System.currentTimeMillis());
                sender.send(message);
                System.out.println("Joining game as: " + username);

                // Update UI to "Waiting for players..."
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

                // Switch to Play Game tab
                JTabbedPane tabbedPane = (JTabbedPane) frame.getContentPane().getComponent(0);
                tabbedPane.setSelectedIndex(1);
            } catch (JMSException ex) {
                System.err.println("Failed to send JOIN message: " + ex);
                JOptionPane.showMessageDialog(frame, "Failed to join game: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(frame, "Failed to submit answer: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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

        System.out.println("Game " + gameId + " started with players: " + playersList + " and cards: " + cardsString);

        gamePanel.removeAll();
        gamePanel.setLayout(new BorderLayout());

        // Center: Display card images
        JPanel cardsPanel = new JPanel();
        cardsPanel.setLayout(new FlowLayout());
        String[] cards = cardsString.split(",");
        System.out.println("Loading cards: " + Arrays.toString(cards));

        for (String card : cards) {
            String cardLower = card.toLowerCase();
            File imageFile = new File("images/cards/" + cardLower + ".gif");

            System.out.println("Trying to load: " + imageFile.getAbsolutePath());
            System.out.println("File exists: " + imageFile.exists());

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

        // East: Display players list
        JPanel playersPanel = new JPanel();
        playersPanel.setLayout(new BoxLayout(playersPanel, BoxLayout.Y_AXIS));
        playersPanel.setBorder(BorderFactory.createTitledBorder("Players"));
        String[] players = playersList.split(",");
        for (String player : players) {
            JLabel playerLabel = new JLabel(player);
            playersPanel.add(playerLabel);
        }
        gamePanel.add(playersPanel, BorderLayout.EAST);

        // South: Input field and buttons
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

        // North: Game ID label
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

        System.out.println("Game " + gameId + " over. Winner: " + winner + ", Answer: " + answer);

        // Clear and update UI
        gamePanel.removeAll();
        gamePanel.setLayout(new BoxLayout(gamePanel, BoxLayout.Y_AXIS));

        // Winner message
        JLabel winnerLabel = new JLabel("Winner of game " + gameId + " is " + winner + "!");
        winnerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        winnerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Winning expression
        JLabel answerLabel = new JLabel("Winning Expression: " + answer);
        answerLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        answerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        gamePanel.add(Box.createVerticalGlue());
        gamePanel.add(winnerLabel);
        gamePanel.add(Box.createRigidArea(new Dimension(0, 20)));
        gamePanel.add(answerLabel);
        gamePanel.add(Box.createVerticalGlue());

        gamePanel.revalidate();
        gamePanel.repaint();

        // Switch to Play Game tab
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