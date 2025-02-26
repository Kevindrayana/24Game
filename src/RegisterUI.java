import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.Naming;
import java.rmi.RemoteException;

public class RegisterUI {
    private JFrame frame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private Auth authenticator;

    public RegisterUI() {
        try {
            authenticator = (Auth) Naming.lookup("Authenticator");
        } catch (Exception e) {
            System.err.println("Failed accessing RMI: " + e);
        }
    }
    public void paint() {
        frame = new JFrame("Register");

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel usernamePanel = new JPanel();
        usernamePanel.setLayout(new BoxLayout(usernamePanel, BoxLayout.Y_AXIS));
        JLabel usernameLabel = new JLabel("Username");
        usernameField = new JTextField(20);
        usernamePanel.add(usernameLabel);
        usernamePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        usernamePanel.add(usernameField);
        usernamePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel passwordPanel = new JPanel();
        passwordPanel.setLayout(new BoxLayout(passwordPanel, BoxLayout.Y_AXIS));
        JLabel passwordLabel = new JLabel("Password");
        passwordField = new JPasswordField(20);
        passwordPanel.add(passwordLabel);
        passwordPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        passwordPanel.add(passwordField);
        passwordPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel confirmPasswordPanel = new JPanel();
        confirmPasswordPanel.setLayout(new BoxLayout(confirmPasswordPanel, BoxLayout.Y_AXIS));
        JLabel confirmPasswordLabel = new JLabel("Confirm Password");
        JPasswordField confirmPasswordField = new JPasswordField(20);
        confirmPasswordPanel.add(confirmPasswordLabel);
        confirmPasswordPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        confirmPasswordPanel.add(confirmPasswordField);
        confirmPasswordPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(usernamePanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(passwordPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(confirmPasswordPanel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton registerButton = new JButton("Register");
        JButton backButton = new JButton("Back to Login");
        buttonPanel.add(registerButton);
        buttonPanel.add(backButton);

        registerButton.addActionListener(new RegisterButtonListener());
        backButton.addActionListener(new BackButtonListener());


        frame.add(mainPanel, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    class BackButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            frame.dispose();
            new LoginUI().paint();
        }
    }

    class RegisterButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText();
            String password = new String (passwordField.getPassword());
            try {
                authenticator.register(username, password);
            } catch (RemoteException ex) {
                throw new RuntimeException(ex);
            };
        }
    }
}