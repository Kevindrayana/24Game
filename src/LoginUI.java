import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.Naming;
import java.rmi.RemoteException;

public class LoginUI {
    private JFrame frame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private Auth authenticator;
    public LoginUI() {
        try {
            authenticator = (Auth) Naming.lookup("Authenticator");
        } catch (Exception e) {
            System.err.println("Failed accessing RMI: " + e);
        }
    }
    public void paint() {
        frame = new JFrame("Login");

        // Create mainPanel with BoxLayout for vertical arrangement
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add padding

        JPanel usernamePanel = new JPanel();
        usernamePanel.setLayout(new BoxLayout(usernamePanel, BoxLayout.Y_AXIS));
        JLabel usernameLabel = new JLabel("Username");
        usernameField = new JTextField(20);
        usernamePanel.add(usernameLabel);
        usernamePanel.add(Box.createRigidArea(new Dimension(0, 5))); // Add spacing
        usernamePanel.add(usernameField);
        usernamePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel passwordPanel = new JPanel();
        passwordPanel.setLayout(new BoxLayout(passwordPanel, BoxLayout.Y_AXIS));
        JLabel passwordLabel = new JLabel("Password");
        passwordField = new JPasswordField(20);
        passwordPanel.add(passwordLabel);
        passwordPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Add spacing
        passwordPanel.add(passwordField);
        passwordPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add components to mainPanel with spacing
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(usernamePanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(passwordPanel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(new LoginButtonListener());
        JButton signUpButton = new JButton("Sign Up");
        signUpButton.addActionListener(new SignUpButtonListener());
        buttonPanel.add(loginButton);
        buttonPanel.add(signUpButton);

        frame.add(mainPanel, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    class SignUpButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            frame.dispose();
            new RegisterUI().paint();
        }
    }

    class LoginButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
           String username = usernameField.getText();
           String password = new String (passwordField.getPassword());
            try {
                if (authenticator.login(username, password) == LoginStatus.SUCCESS) {
                    frame.dispose();
                    new MainUI(username).paint();
                } else if (authenticator.login(username, password) == LoginStatus.USER_NOT_FOUND){
                    frame.dispose();
                    new ErrorUI("Login failed: User not found", "login").paint();
                } else if (authenticator.login(username, password) == LoginStatus.INVALID_CREDENTIALS){
                    frame.dispose();
                    new ErrorUI("Login failed: Invalid Credentials", "login").paint();
                } else if (authenticator.login(username, password) == LoginStatus.ALREADY_LOGGED_IN){
                    frame.dispose();
                    new ErrorUI("Login failed: You are already logged in", "login").paint();
                } else {
                    frame.dispose();
                    new ErrorUI("Login failed: Server error", "login").paint();
                }
            } catch (RemoteException ex) {
                throw new RuntimeException(ex);
            };
        }
    }
}