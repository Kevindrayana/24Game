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

public class MainUI extends JMSHandler implements MessageListener {
    private JFrame frame;
    private String username;
    private Auth authenticator;

    public MainUI(String username) throws NamingException, JMSException {
        super("localhost");
        try {
            this.username = username;
            authenticator = (Auth) Naming.lookup("Authenticator");
            queueReceiver.setMessageListener(this);  // Set the listener
        } catch (Exception e) {
            System.err.println("Failed accessing RMI: " + e);
        }
    }

    public void paint() {
        frame = new JFrame("JPoker 24-Game");
        frame.setPreferredSize(new Dimension(800, 600));

        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel userProfilePanel = createUserProfilePanel();
        tabbedPane.addTab("User Profile", userProfilePanel);

        // placeholder game tab
        JPanel playGamePanel = createPlayGamePanel();
        tabbedPane.addTab("Play Game", playGamePanel);

        JPanel leaderBoardPanel = createLeaderboardPanel();
        tabbedPane.addTab("Leader Board", leaderBoardPanel);

        JPanel logoutPanel = createLogoutPanel();
        tabbedPane.addTab("Logout", logoutPanel);

        frame.add(tabbedPane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
                createSession();
                createSender();
                TextMessage message = session.createTextMessage();
                message.setStringProperty("type", "JOIN");
                message.setText(username);
                queueSender.send(message);
                System.out.println("Sending message..." + message.getText());
            } catch (Exception ex) {
                System.err.println(ex);
            }
        }
    }

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                String type = textMessage.getStringProperty("type");
                System.out.println("getting msg..." + textMessage.getText());
            }
        } catch (JMSException e) {
            System.err.println("Error processing message: " + e);
        }
    }
}