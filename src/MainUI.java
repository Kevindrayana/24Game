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

public class MainUI {
    private JFrame frame;
    private String username;
    private Auth authenticator;

    public MainUI(String username) {
        this.username = username;

        try {
            authenticator = (Auth) Naming.lookup("Authenticator");
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
        tabbedPane.addTab("Play Game", new JPanel());

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

    private JPanel createLeaderboardPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        List<LeaderboardEntry> leaderboard = new ArrayList<>();
        leaderboard.add(new LeaderboardEntry(1, "Alice", 15, 20, 30.5));
        leaderboard.add(new LeaderboardEntry(2, "Bob", 13, 18, 32.1));
        leaderboard.add(new LeaderboardEntry(3, "Charlie", 12, 17, 33.0));
        leaderboard.add(new LeaderboardEntry(4, "David", 11, 16, 34.2));
        leaderboard.add(new LeaderboardEntry(5, "Eve", 10, 15, 35.7));
        leaderboard.add(new LeaderboardEntry(6, "Frank", 9, 14, 36.8));
        leaderboard.add(new LeaderboardEntry(7, "Grace", 8, 13, 37.9));
        leaderboard.add(new LeaderboardEntry(8, "Heidi", 7, 12, 39.0));
        leaderboard.add(new LeaderboardEntry(9, "Ivan", 6, 11, 40.2));
        leaderboard.add(new LeaderboardEntry(10, username, 5, 10, 41.3));

        String[] columnNames = { "Rank", "Username", "Wins", "Games Played", "Avg Time to Win (sec)" };
        Object[][] data = new Object[leaderboard.size()][5];
        for (int i = 0; i < leaderboard.size(); i++) {
            LeaderboardEntry entry = leaderboard.get(i);
            data[i][0] = entry.getRank();
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

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(panel, BorderLayout.CENTER);
        return mainPanel;
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

}