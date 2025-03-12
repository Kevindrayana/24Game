import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;

public class ErrorUI {
    private JFrame frame;
    private String errorMessage;
    private String previousPage; // to know which page to go back to

    public ErrorUI(String errorMessage, String previousPage) {
        this.errorMessage = errorMessage;
        this.previousPage = previousPage;
    }

    public void paint() {
        frame = new JFrame("Error");

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Error icon
        JLabel iconLabel = new JLabel(UIManager.getIcon("OptionPane.errorIcon"));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(iconLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Error message
        JLabel messageLabel = new JLabel(errorMessage);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(messageLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Back button
        JButton backButton = new JButton("Go Back");
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.addActionListener(new BackButtonListener());
        mainPanel.add(backButton);

        frame.add(mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    class BackButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            frame.dispose();
            try {
                if (previousPage.equals("login")) {
                    new LoginUI().paint();
                } else if (previousPage.equals("register")) {
                    new RegisterUI().paint();
                } else { // fail in logout
                    // in this case, previousPage is the username
                    new MainUI(previousPage).paint();
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }

        }
    }
}