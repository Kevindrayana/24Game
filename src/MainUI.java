import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.Naming;
import java.rmi.RemoteException;

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

        // Other tabs (placeholder panels for now)
        tabbedPane.addTab("Play Game", new JPanel());
        tabbedPane.addTab("Leader Board", new JPanel());

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

//        // Profile Icon (using a placeholder circle)
//        JPanel iconPanel = new JPanel() {
//            @Override
//            protected void paintComponent(Graphics g) {
//                super.paintComponent(g);
//                g.setColor(new Color(100, 180, 100));
//                g.fillOval(0, 0, 100, 100);
//            }
//        };
//        iconPanel.setPreferredSize(new Dimension(100, 100));
//        iconPanel.setMaximumSize(new Dimension(100, 100));
//        iconPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Username display
        JLabel usernameLabel = new JLabel("Welcome, " + username);
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 24));
        usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add components
        panel.add(Box.createVerticalGlue());
//        panel.add(iconPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(usernameLabel);
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
        public void actionPerformed(ActionEvent e){
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